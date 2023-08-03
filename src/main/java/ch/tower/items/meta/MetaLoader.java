package ch.tower.items.meta;

import org.json.simple.JSONObject;

import java.lang.reflect.InvocationTargetException;

public class MetaLoader {

    //    {"MetaType":"SkullMeta","Meta":{"Owner":"32234434-de3b-4ee6-8498-6635c691ba16"}}
    public Meta load(JSONObject json){
        try {
            String type = (String)json.get("MetaType");
            if(type!=null&&!type.isEmpty()){
                return (Meta)Class.forName(this.getClass().getPackage().getName()+"."+type).getConstructor(JSONObject.class).newInstance((JSONObject)json.get("Meta"));
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            System.err.println("Can't load Meta with JSON: \n" + json);
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject unload(Meta m){
        JSONObject j = new JSONObject();
        JSONObject meta = m.toJson();
        if(!meta.isEmpty()){
            j.put("MetaType", m.getType().getClazz());
            j.put("Meta", m.toJson());
        }
        return j;
    }

}
