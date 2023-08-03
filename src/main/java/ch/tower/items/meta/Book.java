package ch.tower.items.meta;

import ch.luca008.SpigotApi.Utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.json.simple.JSONObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;

public class Book implements Meta {

    private String author = null;
    private String title = null;
    private BookMeta.Generation generation = BookMeta.Generation.TATTERED;
    private ArrayList<String> pages = new ArrayList<>();

    public Book(JSONObject json){
        if(json.containsKey("Author")){
            author = (String)json.get("Author");
        }
        if(json.containsKey("Title")){
            title = (String)json.get("Title");
        }
        if(json.containsKey("Generation")){
            try{
                generation = BookMeta.Generation.valueOf((String)json.get("Generation"));
            }catch(Exception e){
                System.err.println("Invalid generation tag : " + (String)json.get("Generation"));
            }
        }
        if(json.containsKey("Pages")){
            JSONObject jarr = (JSONObject) json.get("Pages");
            for (Object o : jarr.keySet()) {
                try{
                    pages.add(Integer.parseInt(o.toString()),(String)jarr.get(o));
                }catch(NumberFormatException nfe){
                    System.err.println("BookMeta, can't read " + o + " as an integer.");
                }
            }
        }
    }
    public Book(String author, String title, BookMeta.Generation generation, String...pages){
        this.author = author;
        this.title = title;
        this.generation = generation;
        Collections.addAll(this.pages, pages);
    }
    public Book(BookMeta bookMeta){
        this.author = bookMeta.hasAuthor()?bookMeta.getAuthor():null;
        this.title = bookMeta.hasTitle()?bookMeta.getTitle():null;
        this.generation = bookMeta.hasGeneration()?bookMeta.getGeneration(): BookMeta.Generation.TATTERED;
        this.pages = new ArrayList<>(bookMeta.getPages());
    }

    private ItemStack _apply(ItemStack item, String o){
        if(item!=null){
            if(!item.hasItemMeta()){
                item.setItemMeta(Bukkit.getItemFactory().getItemMeta(item.getType()));
            }
            if(item.getItemMeta() instanceof BookMeta){
                BookMeta bm = (BookMeta) item.getItemMeta();
                if(this.author!=null&&!this.author.isEmpty()){
                    bm.setAuthor(this.author.replace("{P}", o==null?"{P}":o));
                }
                if(this.title!=null&&!this.title.isEmpty()){
                    bm.setTitle(this.title.replace("{P}", o==null?"{P}":o));
                }
                if(this.generation!=null){
                    bm.setGeneration(this.generation);
                }
                if(this.pages!=null){
                    bm.setPages(this.pages);
                }
                item.setItemMeta(bm);
            }
        }
        return item;
    }
    @Override
    public ItemStack apply(ItemStack item) {
        return _apply(item, null);
    }

    public ItemStack applyForPlayer(ItemStack item, String owner){
        return _apply(item,owner);
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        if(this.author!=null&&!this.author.isEmpty()){
            json.put("Author",this.author);
        }
        if(this.title!=null&&!this.title.isEmpty()){
            json.put("Title",this.title);
        }
        if(this.generation!=null){
            if(this.generation!=BookMeta.Generation.TATTERED){
                json.put("Generation",this.generation.name());
            }
        }
        if(this.pages!=null&&!this.pages.isEmpty()){
            JSONObject pages = new JSONObject();
            int i = 0;
            for (String page : this.pages) {
                pages.put(i,page);
                i++;
            }
            json.put("Pages",pages);
        }
        return json;
    }

    @Override
    public MetaType getType() {
        return MetaType.BOOK;
    }

    @Override
    public boolean hasSameMeta(ItemStack item, @Nullable OfflinePlayer player) {
        if(item!=null&&item.getItemMeta() instanceof BookMeta){
            BookMeta bm = (BookMeta) item.getItemMeta();
            String playername = player==null?"{P}":player.getName();
            String localTitle = this.title==null?null:this.title.replace("{P}", playername);
            String localAuthor = this.author==null?null:this.author.replace("{P}", playername);
            if(!(localTitle!=null?(bm.hasTitle()&&bm.getTitle().equals(localTitle)):!bm.hasTitle()))return false;
            if(localAuthor!=null&&!localAuthor.isEmpty()){//checks only if author is set into config
                if(!bm.hasAuthor()||!bm.getAuthor().equals(localAuthor))return false;
            }
            if(!StringUtils.equalLists(this.pages, bm.getPages()))return false;
            return true;
        }
        return false;
    }

    public static boolean hasMeta(ItemStack item){
        if(item!=null&&item.getItemMeta() instanceof BookMeta){
            BookMeta bm = (BookMeta) item.getItemMeta();
            if(bm.hasTitle())return true;
            if(bm.hasAuthor())return true;
            if(bm.getPageCount()>0)return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Book{" +
                "author='" + author + '\'' +
                ", title='" + title + '\'' +
                ", generation=" + generation +
                ", pages=" + pages +
                '}';
    }
}
