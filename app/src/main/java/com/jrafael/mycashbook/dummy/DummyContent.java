package com.jrafael.mycashbook.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<DummyItem> ITEMS = new ArrayList<DummyItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        //String example ="[[Categorias, Totales, Por Día], [Comida, 4650.2, 89.42692307692307], [Impuestos y servicios, 6758.570000000001, 129.97250000000003], [Cosas , 1320.0, 25.384615384615383], [Joda, 2583.0, 49.67307692307692], [Kiosco, 1045.0, 20.096153846153847], [Auto, 1788.25, 34.38942307692308], [PalmaTools, 943.3, 18.140384615384615], [Yeyo, 161.0, 3.0961538461538463], [Cena, 1523.0, 29.28846153846154], [Credito, 1612.34, 31.00653846153846], [Consumido, 28606.650000000005, 550.1278846153847], [Total, 26994.310000000005, 519.1213461538463]]"

        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static DummyItem createDummyItem(int position) {
        return new DummyItem(String.valueOf(position), "Item " + position, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public final String id;
        public final String content;
        public final String details;

        public DummyItem(String id, String content, String details) {
            this.id = id;
            this.content = content;
            this.details = details;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
