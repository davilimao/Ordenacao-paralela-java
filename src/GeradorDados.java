import java.util.Random;

/**
 * Gera os vetores de entrada usados nos testes.
 * Os valores ficam entre 0 e n-1 (necessario para o Counting Sort).
 */
public class GeradorDados {

    public static final String ALEATORIO = "aleatorio";
    public static final String ORDENADO = "ordenado";
    public static final String INVERSO = "inverso";

    // semente fixa para que os testes possam ser repetidos com os mesmos dados
    private static final Random random = new Random(42);

    public static int[] gerar(String tipo, int tamanho) {
        int[] v = new int[tamanho];
        if (tipo.equals(ALEATORIO)) {
            for (int i = 0; i < tamanho; i++) {
                v[i] = random.nextInt(tamanho);
            }
        } else if (tipo.equals(ORDENADO)) {
            for (int i = 0; i < tamanho; i++) {
                v[i] = i;
            }
        } else if (tipo.equals(INVERSO)) {
            for (int i = 0; i < tamanho; i++) {
                v[i] = tamanho - 1 - i;
            }
        }
        return v;
    }

    public static boolean estaOrdenado(int[] v) {
        for (int i = 1; i < v.length; i++) {
            if (v[i - 1] > v[i]) {
                return false;
            }
        }
        return true;
    }
}
