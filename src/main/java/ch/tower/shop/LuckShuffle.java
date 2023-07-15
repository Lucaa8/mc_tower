package ch.tower.shop;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.tower.TowerPlayer;
import ch.tower.managers.ShopMenuManager;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Random;

public class LuckShuffle {

    private enum PerkType {
        ITEM, EFFECT, OTHER;
    }

    private static class Perk {
        private String name;
        private double probability; //between 0 and 1
        private PerkType type;

        public static Perk load(JSONApi.JSONReader json) {
            Perk p = new Perk();
            p.name = json.getString("Name");
            p.type = PerkType.valueOf(json.getString("Type"));
            p.probability = json.getDouble("Probability");
            //maybe p.value = une classe qui implémente une interface (p.ex ItemPerk si c'est une épée, etc.. EffectPerk si c'est une potion...)
            return p;
        }

        //A voir comment appliquer si le joueur possède deja la perk selectionnée. Liste des perks ajoutées sur chaque joueur puis reset a la mort?
        public void apply(TowerPlayer player) {
            //TODO: PERKS
        }
    }

    private static final List<Perk> perks;

    static {
        JSONApi.JSONReader r = SpigotApi.getJSONApi().readerFromFile(ShopMenuManager.SHOP_FILE);
        perks = ((List<Object>) r.getArray("Luck-Shuffle")).stream()
                .filter(JSONObject.class::isInstance).map(JSONObject.class::cast)
                .map(SpigotApi.getJSONApi()::getReader)
                .map(Perk::load)
                .toList();
    }

    //idées de perks
    //Material.CRYING_OBSIDIAN;
    //Material.IRON_CHESTPLATE;
    //plus de vie max
    //effet constant de regen, speed, haste
    //multiplicateur d'argent sur les kills points?
    //jusqua la mort.
    public static void apply(TowerPlayer player)
    {
        if(player == null) return;
        Perk p = getRandomPerk();
        //check si le joueur possède deja cet avantage, sinon soit do while, soit rapeller apply
        getRandomPerk().apply(player);
    }

    //Testé et fonctionnel
    private static Perk getRandomPerk()
    {

        Random random = new Random();
        double randomValue = random.nextDouble();

        double cumulativeProba = 0.0;
        for (Perk p : perks) {
            cumulativeProba += p.probability;
            if (randomValue < cumulativeProba) {
                return p;
            }
        }

        // impossible but who knows, get the last perk in case
        return perks.get(perks.size() - 1);
    }

}
