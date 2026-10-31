import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Bubble Sort.
 * Serial: versao classica com parada antecipada (se nao houve troca, ja esta ordenado).
 * Paralelo: Odd-Even Transposition Sort. Em cada fase as threads comparam
 * pares (par,impar) ou (impar,par) que nao se sobrepoem, entao podem rodar ao mesmo tempo.
 */
public class BubbleSort implements Ordenador {

    @Override
    public String getNome() {
        return "BubbleSort";
    }

    @Override
    public void ordenarSerial(int[] v) {
        int n = v.length;
        for (int i = 0; i < n - 1; i++) {
            boolean trocou = false;
            for (int j = 0; j < n - 1 - i; j++) {
                if (v[j] > v[j + 1]) {
                    int aux = v[j];
                    v[j] = v[j + 1];
                    v[j + 1] = aux;
                    trocou = true;
                }
            }
            if (!trocou) {
                break;
            }
        }
    }

    @Override
    public void ordenarParalelo(int[] v, int numThreads) throws Exception {
        int n = v.length;
        ExecutorService pool = Executors.newFixedThreadPool(numThreads);
        try {
            for (int fase = 0; fase < n; fase++) {
                int inicio = fase % 2; // fase par comeca no 0, fase impar comeca no 1
                int qtdPares = (n - inicio) / 2;
                int paresPorThread = (qtdPares + numThreads - 1) / numThreads;

                List<Callable<Boolean>> tarefas = new ArrayList<>();
                for (int t = 0; t < numThreads; t++) {
                    final int parIni = t * paresPorThread;
                    final int parFim = Math.min(parIni + paresPorThread, qtdPares);
                    if (parIni >= parFim) {
                        break;
                    }
                    tarefas.add(() -> {
                        boolean trocou = false;
                        for (int p = parIni; p < parFim; p++) {
                            int j = inicio + 2 * p;
                            if (v[j] > v[j + 1]) {
                                int aux = v[j];
                                v[j] = v[j + 1];
                                v[j + 1] = aux;
                                trocou = true;
                            }
                        }
                        return trocou;
                    });
                }
                // invokeAll espera todas as threads terminarem a fase (funciona como barreira)
                pool.invokeAll(tarefas);
            }
        } finally {
            pool.shutdown();
        }
    }
}
