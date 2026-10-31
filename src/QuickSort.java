import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Quick Sort com pivo no meio do intervalo (evita o pior caso em vetores ja ordenados).
 * Paralelo: cada particao vira uma tarefa no ForkJoinPool enquanto for grande;
 * abaixo do LIMITE ordena de forma serial para nao criar tarefas demais.
 */
public class QuickSort implements Ordenador {

    private static final int LIMITE = 5000;

    @Override
    public String getNome() {
        return "QuickSort";
    }

    @Override
    public void ordenarSerial(int[] v) {
        quickSort(v, 0, v.length - 1);
    }

    private static void quickSort(int[] v, int esq, int dir) {
        if (esq < dir) {
            int p = particionar(v, esq, dir);
            quickSort(v, esq, p);
            quickSort(v, p + 1, dir);
        }
    }

    // Particionamento de Hoare usando o elemento do meio como pivo
    private static int particionar(int[] v, int esq, int dir) {
        int pivo = v[esq + (dir - esq) / 2];
        int i = esq - 1;
        int j = dir + 1;
        while (true) {
            do {
                i++;
            } while (v[i] < pivo);
            do {
                j--;
            } while (v[j] > pivo);
            if (i >= j) {
                return j;
            }
            int aux = v[i];
            v[i] = v[j];
            v[j] = aux;
        }
    }

    @Override
    public void ordenarParalelo(int[] v, int numThreads) {
        ForkJoinPool pool = new ForkJoinPool(numThreads);
        try {
            pool.invoke(new TarefaQuick(v, 0, v.length - 1));
        } finally {
            pool.shutdown();
        }
    }

    private static class TarefaQuick extends RecursiveAction {
        private final int[] v;
        private final int esq;
        private final int dir;

        TarefaQuick(int[] v, int esq, int dir) {
            this.v = v;
            this.esq = esq;
            this.dir = dir;
        }

        @Override
        protected void compute() {
            if (dir - esq < LIMITE) {
                quickSort(v, esq, dir);
                return;
            }
            int p = particionar(v, esq, dir);
            invokeAll(new TarefaQuick(v, esq, p), new TarefaQuick(v, p + 1, dir));
        }
    }
}
