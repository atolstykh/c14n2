package ru.relex.c14n2.util;

import java.util.*;

/**
 * Class contains function for search support of exists node prefixes declaratoins
 * firstKey - prefix in the original document
 * secondKey - namespace uri
 * level - element depth in the document tree
 */
public class PrefixesContainer {


    private Map<String,LinkedList<String>>  prefixMap;

    private Map<Integer,LinkedList<String>> prefDefLevel;

    public PrefixesContainer() {
        prefixMap = new HashMap<String, LinkedList<String>>();
        prefDefLevel = new HashMap<Integer, LinkedList<String>>();
    }

    /**
     * xmlns:firstKey="URI"
     * @param firstKey
     * @param secondKey
     * @param level
     */
    public void definePrefix(String firstKey, String secondKey, Integer level) {

        if (!prefixMap.containsKey(firstKey)){
            prefixMap.put(firstKey,new LinkedList<String>());
        }

        if (!prefDefLevel.containsKey(level)){
            prefDefLevel.put(level, new LinkedList<String>());
        }

        prefixMap.get(firstKey).push(secondKey);
        prefDefLevel.get(level).push(firstKey);
    }


    /**
     * search prefix declaration from the level and below
     * @param firstKey
     * @return
     */
    public String getByFirstKey(String firstKey) {
        LinkedList<String> list = prefixMap.get(firstKey);
        if (list==null || list.isEmpty()) {
            return null;
        }
        return list.peek();
    }


    /**
     * delete prefix information defined at level.
     * (while processing element end)
     * @param level
     */
    public void deleteLevel(Integer level) {
        LinkedList<String> list = prefDefLevel.get(level);
        if (list!=null) {
            for (String firstKey : list) {
                prefixMap.get(firstKey).pop();
            }
            list.clear();
        }
    }

}
