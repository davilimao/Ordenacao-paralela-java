import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Counting Sort (para inteiros nao negativos).
 * Paralelo: cada thread conta os valores de um pedaco do vetor num vetor de
 * contagem proprio (sem compartilhar nada). Depois as contagens sao somadas
 * e o vetor e reescrito, tambem dividido entre as threads.
 */
public class CountingSort implements Ordenador {

    @Override
    public String getNome() {
        return "CountingSort";
    }

    @Override
    public void ordenarSerial(int[] v) {
        int max = maior(v);
        int[] cont = new int[max + 1];
        for (int x : v) {
            cont[x]++;
        }
        int pos = 0;
        for (int valor = 0; valor <= max; valor++) {
            for (int c = 0; c < cont[valor]; c++) {
                v[pos++] = valor;
            }
        }
    }

    private static int maior(int[] v) {
        int max = 0;
        for (int x : v) {
            if (x > max) {
                max = x;
            }
        }
        return max;
    }

    @Override
    public void ordenarParalelo(int[] v, int numThreads) throws Exception {
        int n = v.length;
        int max = maior(v);
        ExecutorService pool = Executors.newFixedThreadPool(numThreads);
        try {
            int tamPedaco = (n + numThreads - 1) / numThreads;

            // 1) Contagem local em cada thread
            List<Callable<int[]>> contagens = new ArrayList<>();
            for (int t = 0; t < numThreads; t++) {
                final int ini = t * tamPedaco;
                final int fim = Math.min(ini + tamPedaco, n);
                contagens.add(() -> {
                    int[] local = new int[max + 1];
                    for (int i = ini; i < fim; i++) {
                        local[v[i]]++;
                    }
                    return local;
                });
            }
            int[] cont = new int[max + 1];
            for (var resultado : pool.invokeAll(contagens)) {
                int[] local = resultado.get();
                for (int k = 0; k <= max; k++) {
                    cont[k] += local[k];
                }
            }

            // 2) Posicao inicial de cada valor no vetor final (soma de prefixos)
            int[] inicioValor = new int[max + 1];
            int soma = 0;
            for (int k = 0; k <= max; k++) {
                inicioValor[k] = soma;
                soma += cont[k];
            }

            // 3) Escrita do vetor final: cada thread fica com uma faixa de valores
            int faixa = (max + 1 + numThreads - 1) / numThreads;
            List<Callable<Void>> escritas = new ArrayList<>();
            for (int t = 0; t < numThreads; t++) {
                final int vIni = t * faixa;
                final int vFim = Math.min(vIni + faixa, max + 1);
                escritas.add(() -> {
                    for (int valor = vIni; valor < vFim; valor++) {
                        int pos = inicioValor[valor];
                        for (int c = 0; c < cont[valor]; c++) {
                            v[pos++] = valor;
                        }
                    }
                    return null;
                });
            }
            pool.invokeAll(escritas);
        } finally {
            pool.shutdown();
        }
    }
}
