package com.marketguardians.hexagonbuilder;

import com.marketguardians.hexagonbuilder.model.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class JSONReader {

    enum RegionType {
        KRAJ, OKRES, OBEC
    }

    public void saveToJson(HashMap<String, ArrayList<Hexagon>> hashMap, String filename, RegionType type, String parentId) {
        try {
            FileWriter fileWriter = new FileWriter(filename);
            fileWriter.write(getHexJsonArray(hashMap, type, parentId).toString());
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void okresyMultipleHexDoJednehoJson(ArrayList<String> ids) {
        JSONArray array = new JSONArray();
        JSONParser parser = new JSONParser();
        ids.forEach(id -> {
            try {
                FileReader reader = new FileReader(id + "-okresy-multiplehex.json");
                JSONArray okresArray = (JSONArray) parser.parse(reader);
                okresArray.forEach(o -> {
                    JSONObject object = (JSONObject) o;
                    array.add(object);
                });
            } catch (ParseException | IOException e) {
                e.printStackTrace();
            }
        });
        try {
            FileWriter writer = new FileWriter("all-okresy-multiplehex.json");
            writer.write(array.toString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JSONArray getHexJsonArray(HashMap<String, ArrayList<Hexagon>> hashMap, RegionType type, String parentId) {
        AtomicInteger i = new AtomicInteger();
        JSONArray array = new JSONArray();
        hashMap.forEach((key, value) -> { // prechod po jednom regione
            i.getAndIncrement();
            JSONObject object = new JSONObject();
            object.put("objectId", key);
            object.put("name", value.get(0).getName());
            object.put("path", getPath(type, i.intValue(), parentId));
            JSONArray hexagons = new JSONArray();
            value.forEach(h -> { //for each hexagon
                JSONArray hex = new JSONArray();
                h.getPoints().forEach(point -> { //for each point
                    JSONObject coords = new JSONObject();
                    coords.put("lat", point.getLatitude());
                    coords.put("lon", point.getLongitude());
                    hex.add(coords);
                });
                hexagons.add(hex);
            });
            object.put("hexagons", hexagons);
            array.add(object);
        });
        return array;
    }

    public void regionWithCenterIntoArray(ArrayList<Region> regions, String name, String pathIdFileName) {
        JSONArray array = new JSONArray();
        regions.forEach(reg -> {
            String path = findPath(reg.getId(), pathIdFileName);
            JSONObject object = new JSONObject();
            object.put("objectId", reg.getId());
            object.put("path", path);
            object.put("name", reg.getName());
            JSONObject center = new JSONObject();
            center.put("lat", reg.getCenter().getLatitude());
            center.put("lon", reg.getCenter().getLongitude());
            object.put("center", center);
            array.add(object);
        });
        try {
            FileWriter writer = new FileWriter(name);
            writer.write(array.toJSONString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void hexIntoJsonArray(ArrayList<RegionToJson> regions, String name, String pathIdFileName) {
        JSONArray array = new JSONArray();
        regions.forEach(reg -> {
            String path = findPath(reg.getKey(), pathIdFileName);
            JSONObject object = new JSONObject();
            object.put("objectId", reg.getKey());
            object.put("path", path);
            object.put("name", reg.getHexagons().get(0).getName());
            JSONArray hexagons = new JSONArray();
            reg.getHexagons().forEach(h -> { //for each hexagon
                JSONArray hex = new JSONArray();
                h.getPoints().forEach(point -> { //for each point
                    JSONObject coords = new JSONObject();
                    coords.put("lat", point.getLatitude());
                    coords.put("lon", point.getLongitude());
                    hex.add(coords);
                });
                hexagons.add(hex);
            });
            JSONArray holes = new JSONArray();
            reg.getHoles().forEach(h -> {
                JSONArray hex = new JSONArray();
                h.forEach(point -> { //for each point
                    JSONObject coords = new JSONObject();
                    coords.put("lat", point.getLatitude());
                    coords.put("lon", point.getLongitude());
                    hex.add(coords);
                });
                holes.add(hex);
            });
            JSONArray border = new JSONArray();
            reg.getBorder().forEach(point -> {
                JSONObject coords = new JSONObject();
                coords.put("lat", point.getLatitude());
                coords.put("lon", point.getLongitude());
                border.add(coords);
            });
            object.put("hexagons", hexagons);
            object.put("border", border);
            if (holes.size() > 0) {
                object.put("holes", holes);
            }
            array.add(object);
        });
        try {
            FileWriter writer = new FileWriter(name);
            writer.write(array.toJSONString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String findPath(String id, String pathIdFileName) {
        AtomicReference<String> path = new AtomicReference<>("");
        try {
            FileReader reader = new FileReader(pathIdFileName);
            JSONArray pathIdArray = (JSONArray) new JSONParser().parse(reader);
            pathIdArray.forEach(o -> {
                JSONObject object = (JSONObject) o;
                String objectId = String.valueOf((long) object.get("objectId"));
                if (objectId.equalsIgnoreCase(id)) {
                    path.set((String) object.get("path"));
                }
            });
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        return path.get();
    }

    private String getPath(RegionType type, int i, String parentId) {
        StringBuilder path = new StringBuilder();
        int diff = 7;

        //ak to je kraj, tak mu sprav novy path
        if (type == RegionType.KRAJ) {
            diff -= 2;
            if (i > 9) {
                path = new StringBuilder(String.valueOf(i));
            } else {
                path = new StringBuilder("0" + i);
            }
        } else if (type == RegionType.OKRES) {
            //pokad to je okres, tak nacitaj multiplehex krajov, kde kazdy kraj uz ma svoj path a poskladaj to z toho
            diff -= 4;
            try {
                JSONParser parser = new JSONParser();
                FileReader fileReader = new FileReader("kraje-multiplehex.json");
                JSONArray array = (JSONArray) parser.parse(fileReader);
                for (Object o : array) {
                    JSONObject object = (JSONObject) o;
                    String parentObjectId = (String) object.get("objectId");
                    if (parentObjectId.equalsIgnoreCase(parentId)) {
                        String parentPath = (String) object.get("path");
                        path = new StringBuilder(parentPath.substring(0, 2));
                        if (i > 9) {
                            path.append(i);
                        } else {
                            path.append("0").append(i);
                        }
                    }
                }
            } catch (ParseException | IOException e) {
                e.printStackTrace();
            }
        } else {

        }

        for (int x = 0; x < diff; x++) {
            path.append("0");
        }

        return path.toString();
    }

    // ---- KRAJE ---

    public ArrayList<LocationPolygon> spracujKrajGeojson() {
        return spracujGeoSubor("kraje-geo.json");
    }

    public ArrayList<LocationPolygon> spracujViaceroGeojsonSuborov(int diff, int top, String type) {
        ArrayList<LocationPolygon> arrayList = new ArrayList<>();
        for (int i = 0; i <= top; i+=diff) {
            arrayList.addAll(spracujGeoSubor(type + "-" + i + "-geo.json"));
        }
        return arrayList;
    }

    private ArrayList<LocationPolygon> spracujGeoSubor(String filename) {
        ArrayList<LocationPolygon> arrayList = new ArrayList<>();
        try {
            FileReader reader = new FileReader(filename);
            JSONObject object = (JSONObject) new JSONParser().parse(reader);
            JSONArray features = (JSONArray) object.get("features");
            features.forEach(f -> {
                JSONObject feature = (JSONObject) f;
                JSONObject geometry = (JSONObject) feature.get("geometry");
                JSONArray coordinates = (JSONArray) geometry.get("coordinates");
                JSONObject properties = (JSONObject) feature.get("properties");
                String name = (String) properties.get("nazev");
                String type = (String) geometry.get("type");
                long id = (long) properties.get("objectid");
                LocationPolygon locPol = new LocationPolygon(new ArrayList<>(), name, String.valueOf(id));
                if (type.equalsIgnoreCase("MultiPolygon")) {
                    locPol.setPoints(getMultiPolygonPoints(coordinates));
                } else {
                    locPol.setPoints(getPolygonPoints(coordinates));
                }
//                coordinates.forEach(c -> {
//                    JSONArray coords = (JSONArray) c;
//                    coords.forEach(nejakeArray -> {
//                        JSONArray neco = (JSONArray) nejakeArray;
//
////                        locPol.getPoints().add(new LocationCoordinate2D((double) neco.get(0), (double) neco.get(1)));
//                    });
//                });
                arrayList.add(locPol);
            });
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }

        return arrayList;
    }

    // niektore okresy maju geometry type Polygon
    private ArrayList<LocationCoordinate2D> getPolygonPoints(JSONArray coordinates) {
        ArrayList<LocationCoordinate2D> locationPoints = new ArrayList<>();
        coordinates.forEach(c -> {
            JSONArray coords = (JSONArray) c;
            AtomicInteger i = new AtomicInteger();
            coords.forEach(nejakeArray -> {
                JSONArray neco = (JSONArray) nejakeArray;
                locationPoints.add(new LocationCoordinate2D((double) neco.get(0), (double) neco.get(1)));
            });
        });
        return locationPoints;
    }

    // niektore maju geometry type MultiPolygon .. (RUIAN bullshit)
    private ArrayList<LocationCoordinate2D> getMultiPolygonPoints(JSONArray coordinates) { //napr litomerice
        ArrayList<LocationCoordinate2D> locationPoints = new ArrayList<>();
        coordinates.forEach(c -> {
            JSONArray coords = (JSONArray) c;
            AtomicInteger i = new AtomicInteger();
            coords.forEach(nejakeArray -> {
                JSONArray neco = (JSONArray) nejakeArray;
                neco.forEach(ach -> {
                    JSONArray a = (JSONArray) ach;
                    locationPoints.add(new LocationCoordinate2D((double) a.get(0), (double) a.get(1)));
                });
            });
        });
        return locationPoints;
    }


    //JSON z RUIANu, related response nie je vo formate GeoJSON. Dostan len ids z JSON objektu teda :))
    public void multipleRegionJsonGetIds(String filename, String typ) {
        JSONParser jsonParser = new JSONParser();
        try {
            FileReader reader = new FileReader(filename);
            JSONObject object = (JSONObject) jsonParser.parse(reader);
            JSONArray relatedRecordGroups = (JSONArray) object.get("relatedRecordGroups");
            relatedRecordGroups.forEach(o -> {
                JSONObject group = (JSONObject) o;
                ArrayList<Long> ids = new ArrayList<Long>();
                Long parentId = (Long) group.get("objectId");
                JSONArray records = (JSONArray) group.get("relatedRecords");
                records.forEach(r -> {
                    JSONObject record = (JSONObject) r;
                    Long id = (Long) ((JSONObject) record.get("attributes")).get("objectid");
                    ids.add(id);
                });
                try {

                    FileWriter fileWriter = new FileWriter(typ + "-" + parentId + "-related-ids");
                    ids.forEach(id -> {
                        try {
                            fileWriter.write(id + "\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    fileWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }


    public void fetchAllRegionsAndTheirRelations(Long republikaRuianID) {
        try {
            // 1.   dostan id vsetkych regionov sudrznosti
            ArrayList<Long> regionySoudrznosti = idsVsetkychRegionovSoudrznosti(republikaRuianID);
            regionySoudrznosti.forEach(System.out::println);
            // 2.   na zaklade ids regionov sudrznosti dostan ids krajov (related query)
            ArrayList<Long> krajeIds = idsVsetkychKrajov(regionySoudrznosti);
            System.out.println("KRAJE IDS: " + krajeIds.size());
            // 3.   z ids krajov dostan geojson vsetkych krajov
            getGeoJson(krajeIds, "17", "kraje");
            // 4.   ids vsetkych ORP (related query)
            ArrayList<Long> orpIds = idsVsetkychORP(krajeIds);
            System.out.println("ORP IDs: " + orpIds.size());
            splitGeoFetch(orpIds, "orp", "14");
            // 5.   ids vsetkych POU
            ArrayList<Long> pouIds = idsVsetkychPOU(orpIds);
            // 6. ids vsetkych obci
            ArrayList<Long> obceIds = idsVsetkychObci(pouIds);
            splitGeoFetch(obceIds, "obec", "12");
            createPathFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void zacatOdOrp() {
        try {
            ArrayList<Long> orpIds = idsFromIdFile("ids-orp");
            // 5.   ids vsetkych POU
            ArrayList<Long> pouIds = idsVsetkychPOU(orpIds);
            System.out.println("pou ids size: " + pouIds.size());
            // 6. ids vsetkych obci
            ArrayList<Long> obceIds = idsVsetkychObci(pouIds);
            System.out.println("Obce ids size: " + obceIds.size());
            splitGeoFetch(obceIds, "obec", "12");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * RUIAN server is bullshit and can't handle request for all ORPs at once.
     *
     * @param orpIds
     */
    private void splitGeoFetch(ArrayList<Long> orpIds, String type, String typeId) {
        int buff = 10;
        for (int i = 0; i < orpIds.size(); i += buff) {
            List<Long> ids;
            int diff = buff - 1;
            if (i + diff > orpIds.size() - 1) {
                ids = orpIds.subList(i, orpIds.size());
            } else {
                ids = orpIds.subList(i, i + diff + 1);
            }
            System.out.println("Handling: " + i);
            getGeoJson(ids, typeId, type + "-" + i);
        }
    }

    /**
     * Fetches ids of all "region soudrznosti" region types.
     *
     * @param repId RUIAN ID of republic
     * @return array of ids
     * @throws IOException
     */
    private ArrayList<Long> idsVsetkychRegionovSoudrznosti(Long repId) throws IOException {
        ArrayList<Long> ids = new ArrayList<>();
        try {
            JSONObject object = relatedQuery("19", "32", List.of(repId));
            ids = parseRelatedResponseToIds(object);
            FileWriter writer = new FileWriter("ids-region-sudrznosti");
            ids.forEach(id -> {
                try {
                    writer.write(id.toString() + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (ids.size() > 0) {
            return ids;
        } else {
            throw new IOException("ziadne ids");
        }
    }

    /**
     * Fetches all ids of "kraj" region type from RUIAN, based on related records of "region soudrznosti"
     *
     * @param regionIds array of "region soudrznosti" ids
     * @return array of ids
     * @throws IOException
     */
    private ArrayList<Long> idsVsetkychKrajov(ArrayList<Long> regionIds) throws IOException {
        ArrayList<Long> ids;
        JSONObject object = relatedQuery("18", "31", regionIds);
        ids = parseRelatedResponseToIds(object);
        saveIdsToFile("ids-kraje", ids);
        if (ids.size() > 0) {
            return ids;
        } else {
            throw new IOException("empty ids");
        }
    }

    /**
     * Feteches all ids of "ORP" region type from RUIAN, based on related records of "kraj"
     *
     * @param regionIds array of "kraj" ids
     * @return array of ids
     * @throws IOException
     */
    private ArrayList<Long> idsVsetkychORP(ArrayList<Long> regionIds) throws IOException {
        ArrayList<Long> ids;
        JSONObject object = relatedQuery("17", "27", regionIds);
        ids = parseRelatedResponseToIds(object);
        saveIdsToFile("ids-orp", ids);
        if (ids.size() > 0) {
            return ids;
        } else {
            throw new IOException("empty ids");
        }
    }

    private ArrayList<Long> idsVsetkychPOU(ArrayList<Long> regionIds) throws IOException {
        ArrayList<Long> ids;
        JSONObject object = relatedQuery("14", "26", regionIds);
        ids = parseRelatedResponseToIds(object);
        saveIdsToFile("ids-pou", ids);
        if (ids.size() > 0) {
            return ids;
        } else {
            throw new IOException("ids empty");
        }
    }

    private ArrayList<Long> idsVsetkychObci(ArrayList<Long> regionIds) throws IOException {
        ArrayList<Long> ids;
        JSONObject object = relatedQuery("13", "24", regionIds);
        System.out.println("calling parse ids obci");
        ids = parseRelatedResponseToIds(object);
        saveIdsToFile("ids-obce", ids);
        if (ids.size() > 0) {
            return ids;
        } else {
            throw new IOException("ids empty");
        }
    }

    //TODO: Awaiting for RUIAN API
    private ArrayList<Long> idsVsetkychMomc(ArrayList<Long> regionIds) throws IOException {
        ArrayList<Long> ids;
        JSONObject object = relatedQuery("12", "25", regionIds);
        ids = parseRelatedResponseToIds(object);
        saveIdsToFile("ids-momc", ids);
        if (ids.size() > 0) {
            return ids;
        } else {
            throw new IOException("ids empty");
        }
    }

    private void saveIdsToFile(String filename, ArrayList<Long> ids) {
        try {
            FileWriter writer = new FileWriter(filename);
            ids.forEach(id -> {
                try {
                    writer.write(id.toString() + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send GET request to RUIAN to get related regions of chosen type
     *
     * @param typeId         RUIAN type id of region
     * @param relationshipId relationship ID of region to desired related region
     * @param regionIds      RUIAN IDs String of region in format: "1,2,3,4"
     * @return JSON response
     * @throws IOException
     */
    private JSONObject relatedQuery(String typeId, String relationshipId, List<Long> regionIds) throws IOException {
        StringBuilder regionIdsString = new StringBuilder();
        regionIds.forEach(id -> regionIdsString.append(id).append(","));
        try {
            URL url = new URL("http://ags.cuzk.cz/arcgis/rest/services/RUIAN/Prohlizeci_sluzba_nad_daty_RUIAN/MapServer/" + typeId + "/queryRelatedRecords?objectIds=" + regionIdsString.toString() + "&relationshipId=" + relationshipId + "&outFields=objectId&returnGeometry=false&f=pjson");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            StringBuilder buffer = new StringBuilder();
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
            in.close();
            con.disconnect();
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(buffer.toString());
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        throw new IOException("somethign went wrong");
    }

    /**
     * Parses JSON response of related call
     *
     * @param object
     * @return
     */
    private ArrayList<Long> parseRelatedResponseToIds(JSONObject object) {
        ArrayList<Long> ids = new ArrayList<>();
        JSONArray relatedGroups = (JSONArray) object.get("relatedRecordGroups");
        if (object.containsKey("relatedRecordGroups")) {
            relatedGroups.forEach(e -> {
                JSONObject related = (JSONObject) e;
                JSONArray record = (JSONArray) related.get("relatedRecords");
                record.forEach(r -> {
                    JSONObject rec = (JSONObject) r;
                    long id = (long) ((JSONObject) rec.get("attributes")).get("objectid");
                    ids.add(id);
                });
            });
        } else {
            System.out.println("doesn't contain related record groups");
        }

        return ids;
    }

    /**
     * Fetches geojson from RUIAN
     *
     * @param ids      array of IDs of regions to fetch
     * @param typeId   region type ID (RUIAN) - "parent"
     * @param typeName region type name which will be used in file name
     */
    private void getGeoJson(List<Long> ids, String typeId, String typeName) {
        StringBuilder idsString = new StringBuilder();
        ids.forEach(id -> idsString.append(id).append(","));
        try {
            URL url = new URL("http://ags.cuzk.cz/arcgis/rest/services/RUIAN/Prohlizeci_sluzba_nad_daty_RUIAN/MapServer/" + typeId + "/query?objectIds=" + idsString.toString() + "&geometryType=esriGeometryEnvelope&spatialRel=esriSpatialRelIntersects&outFields=nazev%2C+objectId&returnGeometry=true&returnTrueCurves=false&maxAllowableOffset=&returnIdsOnly=false&returnCountOnly=false&returnZ=false&returnM=false&returnDistinctValues=false&returnExtentOnly=false&featureEncoding=esriDefault&f=geojson");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            StringBuilder buffer = new StringBuilder();
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
            in.close();
            con.disconnect();
            JSONParser parser = new JSONParser();
            JSONObject object = (JSONObject) parser.parse(buffer.toString());
            FileWriter writer = new FileWriter(typeName + "-geo.json");
            writer.write(object.toJSONString());
            writer.flush();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Long> idsFromIdFile(String filename) {
        ArrayList<Long> ids = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line = br.readLine();

            while (line != null) {
                ids.add(Long.valueOf(line));
                line = br.readLine();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return ids;
    }

    public void createPathFiles() {
        krajPaths();
        orpPaths();
        obcePaths();
    }

    private void krajPaths() {
        JSONArray array = new JSONArray();
        ArrayList<Long> ids = idsFromIdFile("ids-kraje");
        AtomicInteger i = new AtomicInteger();
        String postfix = "0000000";
        ids.forEach(id -> {
            i.getAndIncrement();
            String path = "";
            if (i.intValue() > 9) {
                path = i + postfix;
            } else {
                path = "0" + i + postfix;
            }

            JSONObject object = new JSONObject();
            object.put("objectId", id);
            object.put("path", path);
            array.add(object);
        });
        try {
            FileWriter writer = new FileWriter("kraje-paths.json");
            writer.write(array.toString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void orpPaths() {
        ArrayList<Long> ids = idsFromIdFile("ids-kraje");
        try {
            JSONObject relatedOrp = relatedQuery("17", "27", ids);
            JSONArray relatedRecordGroups = (JSONArray) relatedOrp.get("relatedRecordGroups");
            JSONArray pathsArray = new JSONArray();
            relatedRecordGroups.forEach(g -> {
                JSONObject group = (JSONObject) g;
                long parentId = (long) group.get("objectId");
                String parentPath = getKrajPath(parentId);
                JSONArray records = (JSONArray) group.get("relatedRecords");
                AtomicInteger i = new AtomicInteger();
                records.forEach(r -> {
                    i.getAndIncrement();
                    JSONObject record = (JSONObject) ((JSONObject) r).get("attributes");
                    long id = (long) record.get("objectid");
                    String prefix = parentPath.substring(0, 2);
                    String postfix = "";
                    if (i.intValue() > 9) {
                        postfix = i.intValue() + "00000";
                    } else {
                        postfix = "0" + i.intValue() + "00000";
                    }
                    JSONObject pathRecord = new JSONObject();
                    pathRecord.put("objectId", id);
                    pathRecord.put("path", prefix + postfix);
                    pathsArray.add(pathRecord);
                });
            });
            FileWriter writer = new FileWriter("orp-paths.json");
            writer.write(pathsArray.toString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void obcePaths() {
        // skipping POU, but data scheme of RUIAN is ORP -> POU -> city
        ArrayList<Long> ids = idsFromIdFile("ids-orp");
        //hasmap of krajId as key and pou Id as value in array
        HashMap<Long, ArrayList<Long>> orpPouHash = new HashMap<>();
        JSONArray pathsArray = new JSONArray(); //to add path/id objects for final file
        HashMap<Long, Integer> obecPerOrp = new HashMap<>();
        try {
            JSONObject relatedPou = relatedQuery("14", "26", ids);
            ArrayList<Long> pouIds = parseRelatedResponseToIds(relatedPou);
            JSONArray relatedRecordGroupsPou = (JSONArray) relatedPou.get("relatedRecordGroups");
            relatedRecordGroupsPou.forEach(g -> {
                JSONObject group = (JSONObject) g;
                long krajId = (long) group.get("objectId");
                orpPouHash.put(krajId, new ArrayList<>());
                JSONArray records = (JSONArray) group.get("relatedRecords");
                records.forEach(r -> {
                    JSONObject record = (JSONObject) r;
                    long pouId = (long) ((JSONObject) record.get("attributes")).get("objectid");
                    orpPouHash.get(krajId).add(pouId);
                });
            });

            JSONObject relatedObce = relatedQuery("13", "24", pouIds);
            JSONArray relatedRecordGroupsObce = (JSONArray) relatedObce.get("relatedRecordGroups");
            relatedRecordGroupsObce.forEach(g -> {
                JSONObject group = (JSONObject) g;
                //find ORP parent based on POU parent
                long pouParentId = (long) group.get("objectId");
                AtomicLong orpParentId = new AtomicLong();
                orpPouHash.forEach((key, values) -> {
                    values.forEach(value -> {
                        if (value == pouParentId) {
                            orpParentId.set(key);
                        }
                    });
                });
                //for each record create path/id object
                JSONArray records = (JSONArray) group.get("relatedRecords");
                AtomicInteger i = new AtomicInteger();
                if (obecPerOrp.containsKey(orpParentId.longValue())) {
                    i.set(obecPerOrp.get(orpParentId.longValue()));
                } else {
                    obecPerOrp.put(orpParentId.longValue(), 0);
                }
                records.forEach(r -> {
                    i.getAndIncrement();
                    obecPerOrp.replace(orpParentId.longValue(), i.intValue());
                    JSONObject record = (JSONObject) r;
                    long id = (long)  ((JSONObject) record.get("attributes")).get("objectid");
                    String parentPath = getOrpPath(orpParentId.longValue());
                    String prefix = parentPath.substring(0, 4);
                    String postfix;
                    if (i.intValue() > 99) {
                        System.out.println("I: " + i.intValue());
                        postfix = i.intValue() + "00";
                    } else if (i.intValue() > 9) {
                        postfix = "0" + i.intValue() + "00";
                    } else {
                        postfix = "00" + i.intValue() + "00";
                    }
                    JSONObject pathRecord = new JSONObject();
                    pathRecord.put("objectId", id);
                    pathRecord.put("path", prefix + postfix);
                    pathsArray.add(pathRecord);
                });
            });
            FileWriter writer = new FileWriter("obec-paths.json");
            writer.write(pathsArray.toString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkPathDuplicates() {
        try {
            FileReader reader = new FileReader("orp-paths.json");
            JSONParser parser = new JSONParser();
            JSONArray array = (JSONArray) parser.parse(reader);
            Set<String> paths = new HashSet<>();
            array.forEach(o -> {
                JSONObject object = (JSONObject) o;
                String path = (String) object.get("path");
                if (paths.contains(path)) {
                    System.out.println("OBSAHUJE! " + path + object.get("objectId"));
                }
                paths.add(path);
            });
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private String getKrajPath(long id) {
        String path = "";
        try {
            FileReader reader = new FileReader("kraje-paths.json");
            JSONParser parser = new JSONParser();
            JSONArray array = (JSONArray) parser.parse(reader);
            for (Object o: array) {
                JSONObject object = (JSONObject) o;
                long objectId = (long) object.get("objectId");
                if (objectId == id) {
                    path = (String) object.get("path");
                    return path;
                }
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    private String getOrpPath(long id) {
        String path = "";
        try {
            FileReader reader = new FileReader("orp-paths.json");
            JSONParser parser = new JSONParser();
            JSONArray array = (JSONArray) parser.parse(reader);
            for (Object o: array) {
                JSONObject object = (JSONObject) o;
                long objectId = (long) object.get("objectId");
                if (objectId == id) {
                    path = (String) object.get("path");
                    return path;
                }
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        return path;
    }


}

