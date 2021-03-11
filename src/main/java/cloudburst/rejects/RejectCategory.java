package cloudburst.rejects;

import net.minecraft.item.Items;

import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Modules;

public class RejectCategory {
    public static final Category Rejects = new Category("Rejects", Items.DIRT.getDefaultStack());
    
    public static void register() {
        Modules.registerCategory(Rejects);
    }
}
