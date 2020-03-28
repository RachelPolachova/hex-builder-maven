package com.marketguardians.hexagonbuilder;

import com.marketguardians.hexagonbuilder.model.LocationCoordinate2D;
import com.marketguardians.hexagonbuilder.model.LocationPolygon;
import com.marketguardians.hexagonbuilder.model.PathIdTable;
import com.marketguardians.hexagonbuilder.model.RegionDb;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

enum RegionType {
    KRAJ, ORP, POU, OBEC, MOMC
}

public class RUIANService {

    String baseURLString = "http://ags.cuzk.cz/arcgis/rest/services/RUIAN/Prohlizeci_sluzba_nad_daty_RUIAN/MapServer";
    String jsonFormat = "?f=pjson";

    String krajPostfix = "000000000";
    int i = 0;

    public void krajDetails(String filename) {
        InputStream is = RUIANService.class.getClassLoader().getResourceAsStream(filename);
        if (is != null) {
            JSONTokener tokener = new JSONTokener(is);
            JSONArray array = new JSONArray(tokener);
            array.forEach(o -> {
                JSONObject object = (JSONObject) o;
                int id = (int) object.get("id");
                getKVUSCDetails(id);
            });
        } else {
            System.out.println("null pyco");
        }
    }

    public LocationPolygon getLocPolygon(String filename) {
        LocationPolygon polygon = new LocationPolygon(new ArrayList<>(), "ach", "a");
        InputStream is = RUIANService.class.getClassLoader().getResourceAsStream(filename);
        if (is != null) {
            JSONTokener tokener = new JSONTokener(is);
            JSONObject object = new JSONObject(tokener);
            JSONArray features = object.getJSONArray("features");
            features.forEach(f -> {
                JSONObject feature = ((JSONObject) f).getJSONObject("geometry");
                JSONArray coords = feature.getJSONArray("coordinates");
                coords.forEach(c -> {
                    JSONArray coordsArr = (JSONArray) c;
                    coordsArr.forEach(finalnePole -> {
                        JSONArray pole = (JSONArray) finalnePole;
                        LocationCoordinate2D point = new LocationCoordinate2D(pole.getInt(0), pole.getInt(1));
                        polygon.getPoints().add(point);
                    });
                });
            });
        } else {
            System.out.println("null");
        }
        try {
            FileWriter fileWriter = new FileWriter("points-" + filename);
            polygon.getPoints().forEach(point -> {
                try {
                    fileWriter.write(point.getLongitude() + ", " + point.getLatitude() + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return polygon;
    }

    public void joinRegionDbAHexagonPoints(String regionDbFilename, String hexagonPointsFilename, String newFilename) {
        InputStream isHexagons = RUIANService.class.getClassLoader().getResourceAsStream(hexagonPointsFilename);
        InputStream isDb = RUIANService.class.getClassLoader().getResourceAsStream(regionDbFilename);
        if (isHexagons != null && isDb != null) {
            JSONTokener pointsTokener = new JSONTokener(isHexagons);
            JSONTokener dbTokener = new JSONTokener(isDb);
            JSONArray dbArray = new JSONArray(dbTokener);
            JSONArray pointsArray = new JSONArray(pointsTokener);
            dbArray.forEach(e -> {
                JSONObject element = (JSONObject) e;
                long elementId = element.getLong("id");
                for (Object p: pointsArray) {
                    JSONObject pointsEl = (JSONObject) p;
                    if (pointsEl.getLong("id") == elementId) {
                        element.put("points", pointsEl.getJSONArray("points"));
                        return;
                    }
                }
            });
            try {
                FileWriter fileWriter = new FileWriter(newFilename);
                fileWriter.write(dbArray.toString());
                fileWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("one file is null");
        }
    }

    public void spracujKraje(String filename) {
        vytvorSuborSKrajmi(spracujRuianJsonPreDb(filename), "kraje-db.json");
    }

    public void saveOnlyIds(String filename) {
        ArrayList<String> ids = new ArrayList<>();
        InputStream is = RUIANService.class.getClassLoader().getResourceAsStream(filename);
        if (is != null) {
            JSONTokener tokener = new JSONTokener(is);
            JSONArray array = new JSONArray(tokener);
            array.forEach(o -> {
                JSONObject object = (JSONObject) o;
                ids.add(String.valueOf(object.getLong("id")));
            });
        } else {
            System.out.println("null.");
        }
        saveIdsAsFile(ids, filename.substring(0, filename.length() - 5) + "-ids.txt");
    }

    private void saveIdsAsFile(ArrayList<String> ids, String filename) {
        try {
            FileWriter fileWriter = new FileWriter(filename);
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

    }

    public void spracujOrp(String filename, RegionType type) {
        int prefixEnd = 2;
        String pathIdFileName = "";
        String newJsonFileName = "";
        switch (type) {
            case KRAJ:
                pathIdFileName = "";
                newJsonFileName = "kraje-db.json";
                break;
            case ORP:
                pathIdFileName = "./kraje-db-pathId.json";
                newJsonFileName = "orp-db.json";
                prefixEnd = 2;
                break;
            case POU:
                pathIdFileName = "./orp-db-pathId.json";
                newJsonFileName = "pou-db.json";
                prefixEnd = 4;
                break;
            case OBEC:
                pathIdFileName = "./pou-db-pathId.json";
                newJsonFileName = "obec-db.json";
                prefixEnd = 7;
            case MOMC:
                pathIdFileName = "./obec-db-pathId.json";
                newJsonFileName = "momc-db.json";
                prefixEnd = 9;
                break;

        }
        ArrayList<RegionDb> orpArray = new ArrayList<>();
        ArrayList<PathIdTable> pathIdKrajeArray = getPahtIdArray(pathIdFileName);
        i = 0;
        ArrayList<Long> handledIds = new ArrayList<>();
        AtomicInteger n = new AtomicInteger();
        InputStream is = RUIANService.class.getClassLoader().getResourceAsStream(filename);
        if (is != null) {
            JSONTokener tokener = new JSONTokener(is);
            JSONArray relatedRecordGroups = new JSONObject(tokener).getJSONArray("relatedRecordGroups");
            int finalPrefixEnd = prefixEnd;
            relatedRecordGroups.forEach(g -> {
                i = 0;
                JSONObject group = (JSONObject) g;
                try {
                    PathIdTable pathId = findById(group.getLong("objectId"), pathIdKrajeArray);
                    JSONArray records = group.getJSONArray("relatedRecords");
                    records.forEach(r -> {
                        n.getAndIncrement();
                        i++;
                        JSONObject rec = (JSONObject) r;
                        JSONObject attributes = rec.getJSONObject("attributes");
//                        System.out.println("path: " + pathId.getPath() + " substring: " + pathId.getPath().substring(0, 2));
                        String path = pathId.getPath().substring(0, finalPrefixEnd); //prve dva znaky kraju
                        if (type == RegionType.OBEC) {
                            if (i > 99) {
                                path += i;
                            } else if (i > 9) {
                                path += "0" + i;
                            } else {
                                path += "00" + i;
                            }
                        } else {
                            if (i > 9) {
                                path += i;
                            } else {
                                path = path + "0" + i;
                            }
                        }
                        for (int x = path.length(); x <11; x++) {
                            path+="0";
                        }
//                        path+= "00000"; //000 obec 00 momc

                        for (Long handledId : handledIds) {
                            if (attributes.getLong("objectid") == handledId) {
                                System.out.println("!!!!!" + attributes.getLong("objectid"));
                            }
                        }

                        if (handledIds.contains(attributes.getLong("objectid"))) {
                            System.out.println("!!!!!" + attributes.getLong("objectid"));
                        }

                        handledIds.add(attributes.getLong("objectid"));
                        orpArray.add(new RegionDb(attributes.getLong("objectid"), attributes.getLong("kod"), attributes.getString("nazev"), path));
                    });
                } catch (IOException e) {
                    System.out.println("Haven't find any path/id: " + group.getLong("objectId"));
                    e.printStackTrace();
                }
            });
            System.out.println("Recors: " + n);
        } else {
            System.out.println("failed to get orps");
        }
        vytvorSuborSKrajmi(orpArray, newJsonFileName);
    }


    private PathIdTable findById(long id, ArrayList<PathIdTable> array) throws IOException  {
        for (PathIdTable region: array) {
            if (region.getId() == id) {
                return region;
            }
        }
        throw new IOException();
    }

    private ArrayList<PathIdTable> getPahtIdArray(String filename) {
        ArrayList<PathIdTable> table = new ArrayList<>();
        InputStream is = RUIANService.class.getClassLoader().getResourceAsStream(filename);
        if (is != null) {
            JSONTokener tokener = new JSONTokener(is);
            JSONArray array = new JSONArray(tokener);
            array.forEach(pi -> {
                JSONObject object = (JSONObject) pi;
                table.add(new PathIdTable(object.getString("path"), object.getLong("id")));
            });
        } else {
            System.out.println("failed to get path id json");
        }
        return table;
    }

    private ArrayList<RegionDb> spracujRuianJsonPreDb(String filename) {
        ArrayList<RegionDb> regions = new ArrayList<>();
        InputStream is = RUIANService.class.getClassLoader().getResourceAsStream(filename);
        if (is != null) {
            JSONTokener tokener = new JSONTokener(is);
            JSONArray features = new JSONObject(tokener).getJSONArray("features");
            features.forEach(f -> {
                i++;
                JSONObject feature = (JSONObject) f;
                JSONObject object = feature.getJSONObject("attributes");
                long objectId = object.getLong("objectid");
                String name = object.getString("nazev");
                long code = object.getLong("kod");
                String prefix = "";
                if (i > 9) {
                    prefix = String.valueOf(i);
                } else {
                    prefix = "0" + String.valueOf(i);
                }
                regions.add(new RegionDb(objectId, code, name, prefix + krajPostfix));
            });
        } else {
            System.out.println("fail");
        }
        return regions;
    }

    public void createRegionIdPathJson(String filename) {
        InputStream is = RUIANService.class.getClassLoader().getResourceAsStream(filename);
        if (is != null) {
            String name = filename.substring(2, filename.length() - 5); //remove `./` and `.json`
            JSONArray pathIdArray = new JSONArray();
            JSONTokener tokener = new JSONTokener(is);
            JSONArray regions = new JSONArray(tokener);
            regions.forEach(r -> {
                JSONObject reg = (JSONObject) r;
                JSONObject pathId = new JSONObject();
                pathId.put("id", reg.getLong("id"));
                pathId.put("path", reg.getString("path"));
                pathIdArray.put(pathId);
            });
            try {
                FileWriter fileWriter = new FileWriter(name + "-pathId.json");
                fileWriter.write(pathIdArray.toString());
                fileWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("fail");
        }
    }


    private JSONArray krajeDoJson(ArrayList<RegionDb> regions) {
        JSONArray array = new JSONArray();

        regions.forEach(regionDb -> {
            JSONObject object = new JSONObject();
            object.put("id", regionDb.getId());
            object.put("name", regionDb.getName());
            object.put("code", regionDb.getCode());
            object.put("path", regionDb.getPath());
            array.put(object);
        });

        return array;
    }

    private void vytvorSuborSKrajmi(ArrayList<RegionDb> regions, String filename) {
        try {
            FileWriter fileWriter = new FileWriter(filename);
            JSONArray kraje = krajeDoJson(regions);
            fileWriter.write(kraje.toString());
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //VUSC = kraj
    private void getKVUSCDetails(int id) {
        String urlString = "http://ags.cuzk.cz/arcgis/rest/services/RUIAN/Vyhledavaci_sluzba_nad_daty_RUIAN/MapServer/17/query?where=&text=&objectIds=" + id + "&time=&geometry=&geometryType=esriGeometryEnvelope&inSR=&spatialRel=esriSpatialRelIntersects&relationParam=&outFields=&returnGeometry=true&returnTrueCurves=false&maxAllowableOffset=&geometryPrecision=&outSR=&having=&returnIdsOnly=false&returnCountOnly=false&orderByFields=&groupByFieldsForStatistics=&outStatistics=&returnZ=false&returnM=false&gdbVersion=&historicMoment=&returnDistinctValues=false&resultOffset=&resultRecordCount=&queryByDistance=&returnExtentOnly=false&datumTransformation=&parameterValues=&rangeValues=&quantizationParameters=&featureEncoding=esriDefault&f=json";
//                baseURLString + "/17/" + id + jsonFormat;
        try {
            URL url = new URL(urlString);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            String line;
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuffer response = new StringBuffer();
            while ((line = in.readLine()) != null) {
//                response.append(line);
                response.append(line);
            }
            in.close();
            // Read JSON
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONObject attributes = jsonResponse.getJSONArray("features").getJSONObject(0).getJSONObject("attributes");
            String nazev = attributes.getString("nazev");
            System.out.println("ID: " + id + " objectId: " + " nazev: " + nazev);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
