/**
 * Interface comum para todos os algoritmos de ordenacao do trabalho.
 * Cada algoritmo tem uma versao serial e uma versao paralela.
 */
public interface Ordenador {

    String getNome();

    void ordenarSerial(int[] vetor);

    void ordenarParalelo(int[] vetor, int numThreads) throws Exception;
}
