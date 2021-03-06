package odms.commons.model.profile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The full HLA type of a patient
 */
public class HLAType {

    public static final String GENE = "[a-z,A-Z]+";
    public static final String ALELLE = "[0-9]+";
    public static final String SECONDARY_TYPE = GENE + ALELLE;

    private static List<String> primaryGeneList = Arrays.asList("A", "B", "C", "DP", "DQ", "DR");

    private Map<String, Integer> groupX = new HashMap<>();
    private Map<String, Integer> groupY = new HashMap<>();
    private Map<String, Integer> secondaryAntigens = new HashMap<>();


    /**
     * Initialises the keys of the HLA type
     */
    public HLAType() {
        // initialise group X keys
        groupX.put("A", null);
        groupX.put("B", null);
        groupX.put("C", null);
        groupX.put("DP", null);
        groupX.put("DQ", null);
        groupX.put("DR", null);

        // initialise group Y keys
        groupY.put("A", null);
        groupY.put("B", null);
        groupY.put("C", null);
        groupY.put("DP", null);
        groupY.put("DQ", null);
        groupY.put("DR", null);
    }

    /**
     * Initialises the key-value pairs of the HLA type
     *
     * @param xa Group X gene A  Allele
     * @param xb Group X gene B  Allele
     * @param xc Group X gene C  Allele
     * @param xdp Group X gene DP Allele
     * @param xdq Group X gene DQ Allele
     * @param xdr Group X gene DR Allele
     * @param ya Group Y gene A  Allele
     * @param yb Group Y gene B  Allele
     * @param yc Group Y gene C  Allele
     * @param ydp Group Y gene DQ Allele
     * @param ydq Group Y gene DP Allele
     * @param ydr Group Y gene DR Allele
     */
    public HLAType(Integer xa, Integer xb, Integer xc, Integer xdp, Integer xdq, Integer xdr,
            Integer ya, Integer yb, Integer yc, Integer ydp, Integer ydq, Integer ydr) {
        // initialise group X
        groupX.put("A", xa);
        groupX.put("B", xb);
        groupX.put("C", xc);
        groupX.put("DP", xdp);
        groupX.put("DQ", xdq);
        groupX.put("DR", xdr);

        // initialise group Y
        groupY.put("A", ya);
        groupY.put("B", yb);
        groupY.put("C", yc);
        groupY.put("DP", ydp);
        groupY.put("DQ", ydq);
        groupY.put("DR", ydr);
    }

    /**
     * Initialises the primary HLAs from a list of Integers
     *
     * @param hlas list of HLA alleles
     */
    public HLAType(List<Integer> hlas) {
        // initialise group X
        groupX.put("A", hlas.get(0));
        groupX.put("B", hlas.get(1));
        groupX.put("C", hlas.get(2));
        groupX.put("DP", hlas.get(3));
        groupX.put("DQ", hlas.get(4));
        groupX.put("DR", hlas.get(5));

        // initialise group Y
        groupY.put("A", hlas.get(6));
        groupY.put("B", hlas.get(7));
        groupY.put("C", hlas.get(8));
        groupY.put("DP", hlas.get(9));
        groupY.put("DQ", hlas.get(10));
        groupY.put("DR", hlas.get(11));
    }

    public static List<String> getPrimaryGeneList() {
        return primaryGeneList;
    }

    public Map<String, Integer> getGroupX() {
        return groupX;
    }

    public Map<String, Integer> getGroupY() {
        return groupY;
    }

    public Map getSecondaryAntigens() {
        return secondaryAntigens;
    }

    public void setGroupX(Map<String, Integer> map) {
        groupX = map;
    }

    public void setGroupY(Map<String, Integer> map) {
        groupY = map;
    }

    public void setSecondaryAntigens(Map<String, Integer> map) {
        secondaryAntigens = map;
    }

    public void addSecondaryAntigen(String gene, Integer allele) {
        secondaryAntigens.put(gene, allele);
    }
}

