package be.technifutur.erp_finalproject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Totals — calcul des montants d'un devis ou d'une facture")
class TotalsTest {

    // Les valeurs de reference viennent d'un cas reel : 2 x 499.99 + 1 x 449.99,
    // TVA 21 %, remise 10 %. Ce sont celles vérifiées par la collection Postman.
    private static final BigDecimal SUB_TOTAL = new BigDecimal("1449.97");
    private static final BigDecimal TVA_BRUTE = new BigDecimal("304.50");

    @Test
    @DisplayName("remise de 10 % : la TVA et le total sont réduits, le sous-total reste brut")
    void appliqueLaRemise() {
        Totals totals = Totals.of(SUB_TOTAL, TVA_BRUTE, new BigDecimal("10.00"));

        // 304.50 x 0.9
        assertThat(totals.amountTva()).isEqualByComparingTo("274.05");
        // 1449.97 x 0.9 + 274.05 = 1579.023, arrondi a 1579.02
        assertThat(totals.totalPrice()).isEqualByComparingTo("1579.02");
        // le sous-total n'est jamais remise : l'entité stocke le brut et la remise séparément
        assertThat(totals.subTotal()).isEqualByComparingTo(SUB_TOTAL);
    }

    @ParameterizedTest(name = "discount = {0}")
    @NullSource
    @CsvSource({"0.00", "-5.00"})
    @DisplayName("remise nulle, zero ou negative : aucun changement")
    void pasDeRemise(BigDecimal discount) {
        Totals totals = Totals.of(SUB_TOTAL, TVA_BRUTE, discount);

        assertThat(totals.amountTva()).isEqualByComparingTo(TVA_BRUTE);
        assertThat(totals.totalPrice()).isEqualByComparingTo("1754.47");
    }

    @Test
    @DisplayName("remise de 100 % : tout tombe a zero")
    void remiseTotale() {
        Totals totals = Totals.of(SUB_TOTAL, TVA_BRUTE, new BigDecimal("100.00"));

        assertThat(totals.amountTva()).isEqualByComparingTo("0.00");
        assertThat(totals.totalPrice()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("les montants sortent toujours a deux décimales")
    void arrondiADeuxDecimales() {
        Totals totals = Totals.of(new BigDecimal("100.00"), new BigDecimal("21.00"), new BigDecimal("33.33"));

        assertThat(totals.amountTva().scale()).isEqualTo(2);
        assertThat(totals.totalPrice().scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("remise a decimales infinies : le coefficient garde 4 décimales avant l'arrondi final")
    void remiseNonTerminale() {
        // 1/3 en pourcentage : la division n'a pas de representation décimale exacte.
        // Sans l'échelle imposée au coefficient, BigDecimal lèverait ArithmeticException.
        Totals totals = Totals.of(new BigDecimal("100.00"), new BigDecimal("21.00"), new BigDecimal("33.3333"));

        // coeff = 1 - 0.3333 = 0.6667  ->  21 x 0.6667 = 14.0007 -> 14.00
        assertThat(totals.amountTva()).isEqualByComparingTo("14.00");
        // 100 x 0.6667 + 14.00 = 80.67
        assertThat(totals.totalPrice()).isEqualByComparingTo("80.67");
    }

    @Test
    @DisplayName("aucune ligne : tous les montants sont a zero")
    void sansLigne() {
        Totals totals = Totals.of(BigDecimal.ZERO, BigDecimal.ZERO, null);

        assertThat(totals.subTotal()).isEqualByComparingTo("0");
        assertThat(totals.amountTva()).isEqualByComparingTo("0");
        assertThat(totals.totalPrice()).isEqualByComparingTo("0");
    }
}
