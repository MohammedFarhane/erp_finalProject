package be.technifutur.erp_finalproject;

public class SearchPattern {

    private SearchPattern() {}

    public static String like (String value) {
        return (value == null || value.isBlank())
        ? null
        : "%" + value.toLowerCase() + "%";
    }

}
