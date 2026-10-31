import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Merge Sort.
 * Paralelo: as duas metades sao ordenadas em tarefas separadas no ForkJoinPool,
 * depois e feito o merge. Abaixo do LIMITE ordena de forma serial.
 */
public class MergeSort implements Ordenador {

    private static final int LIMITE = 5000;

    @Override
    public String getNome() {
        return "MergeSort";
    }

    @Override
    public void ordenarSerial(int[] v) {
        int[] aux = new int[v.length];
        mergeSort(v, aux, 0, v.length - 1);
    }

    private static void mergeSort(int[] v, int[] aux, int ini, int fim) {
        if (ini >= fim) {
            return;
        }
        int meio = (ini + fim) / 2;
        mergeSort(v, aux, ini, meio);
        mergeSort(v, aux, meio + 1, fim);
        merge(v, aux, ini, meio, fim);
    }

    private static void merge(int[] v, int[] aux, int ini, int meio, int fim) {
        for (int k = ini; k <= fim; k++) {
            aux[k] = v[k];
        }
        int i = ini;
        int j = meio + 1;
        for (int k = ini; k <= fim; k++) {
            if (i > meio) {
                v[k] = aux[j++];
            } else if (j > fim) {
                v[k] = aux[i++];
            } else if (aux[i] <= aux[j]) {
                v[k] = aux[i++];
            } else {
                v[k] = aux[j++];
            }
        }
    }

    @Override
    public void ordenarParalelo(int[] v, int numThreads) {
        int[] aux = new int[v.length];
        ForkJoinPool pool = new ForkJoinPool(numThreads);
        try {
            pool.invoke(new TarefaMerge(v, aux, 0, v.length - 1));
        } finally {
            pool.shutdown();
        }
    }

    private static class TarefaMerge extends RecursiveAction {
        private final int[] v;
        private final int[] aux;
        private final int ini;
        private final int fim;

        TarefaMerge(int[] v, int[] aux, int ini, int fim) {
            this.v = v;
            this.aux = aux;
            this.ini = ini;
            this.fim = fim;
        }

        @Override
        protected void compute() {
            if (fim - ini < LIMITE) {
                mergeSort(v, aux, ini, fim);
                return;
            }
            int meio = (ini + fim) / 2;
            // cada metade usa uma parte diferente do vetor aux, entao nao ha conflito
            invokeAll(new TarefaMerge(v, aux, ini, meio), new TarefaMerge(v, aux, meio + 1, fim));
            merge(v, aux, ini, meio, fim);
        }
    }
}
