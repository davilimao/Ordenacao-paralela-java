import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * "Framework" de testes do trabalho.
 * Para cada algoritmo, tamanho de entrada e tipo de entrada, executa a versao
 * serial e a versao paralela com 2, 4 e 8 threads, 5 amostras cada.
 * Os tempos sao gravados em resultados/resultados.csv (todas as amostras) e
 * resultados/resumo.csv (media, desvio padrao e speedup).
 *
 * Uso: java Main                      -> roda todos os algoritmos
 *      java Main QuickSort MergeSort  -> roda so os algoritmos informados
 * Se os arquivos CSV ja existirem, os novos resultados sao adicionados no final.
 */
public class Main {

    static final int[] TAMANHOS = {1000, 10000, 50000, 100000, 500000, 1000000};
    static final int TAMANHO_MAX_BUBBLE = 50000; // Bubble e O(n^2), acima disso demora demais
    static final String[] TIPOS = {GeradorDados.ALEATORIO, GeradorDados.ORDENADO, GeradorDados.INVERSO};
    static final int[] THREADS = {2, 4, 8};
    static final int AMOSTRAS = 5;

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.US); // ponto como separador decimal no CSV

        int nucleos = Runtime.getRuntime().availableProcessors();
        System.out.println("Nucleos disponiveis: " + nucleos);
        System.out.println("Java: " + System.getProperty("java.version"));

        Ordenador[] algoritmos = {new BubbleSort(), new QuickSort(), new MergeSort(), new CountingSort()};

        aquecerJvm(algoritmos);

        new File("resultados").mkdirs();
        PrintWriter csv = abrirCsv("resultados/resultados.csv",
                "algoritmo,modo,threads,tamanho,tipo_entrada,amostra,tempo_ms");
        PrintWriter resumo = abrirCsv("resultados/resumo.csv",
                "algoritmo,modo,threads,tamanho,tipo_entrada,media_ms,desvio_ms,min_ms,max_ms,speedup");

        List<String> escolhidos = Arrays.asList(args);

        for (Ordenador alg : algoritmos) {
            if (!escolhidos.isEmpty() && !escolhidos.contains(alg.getNome())) {
                continue;
            }
            for (int tamanho : TAMANHOS) {
                if (alg instanceof BubbleSort && tamanho > TAMANHO_MAX_BUBBLE) {
                    continue;
                }
                for (String tipo : TIPOS) {
                    int[] original = GeradorDados.gerar(tipo, tamanho);

                    // execucao serial (threads = 1)
                    double[] temposSerial = medir(alg, original, 1, csv, tamanho, tipo);
                    double mediaSerial = media(temposSerial);
                    gravarResumo(resumo, alg.getNome(), "serial", 1, tamanho, tipo, temposSerial, 1.0);

                    // execucoes paralelas
                    for (int t : THREADS) {
                        double[] tempos = medir(alg, original, t, csv, tamanho, tipo);
                        double speedup = mediaSerial / media(tempos);
                        gravarResumo(resumo, alg.getNome(), "paralelo", t, tamanho, tipo, tempos, speedup);
                    }
                    System.out.printf("%-12s n=%-8d %-9s serial=%.3f ms%n",
                            alg.getNome(), tamanho, tipo, mediaSerial);
                }
            }
        }
        csv.close();
        resumo.close();
        System.out.println("Fim! Arquivos gerados na pasta resultados/");
    }

    /** Abre o CSV em modo de adicao; escreve o cabecalho so se o arquivo for novo. */
    static PrintWriter abrirCsv(String caminho, String cabecalho) throws IOException {
        boolean novo = !new File(caminho).exists();
        PrintWriter pw = new PrintWriter(new FileWriter(caminho, true));
        if (novo) {
            pw.println(cabecalho);
        }
        return pw;
    }

    /** Executa AMOSTRAS vezes e devolve os tempos em milissegundos. threads = 1 significa serial. */
    static double[] medir(Ordenador alg, int[] original, int threads, PrintWriter csv,
                          int tamanho, String tipo) throws Exception {
        double[] tempos = new double[AMOSTRAS];
        String modo = (threads == 1) ? "serial" : "paralelo";
        for (int a = 0; a < AMOSTRAS; a++) {
            int[] v = Arrays.copyOf(original, original.length); // copia fora da medicao

            long inicio = System.nanoTime();
            if (threads == 1) {
                alg.ordenarSerial(v);
            } else {
                alg.ordenarParalelo(v, threads);
            }
            long fim = System.nanoTime();

            if (!GeradorDados.estaOrdenado(v)) {
                throw new RuntimeException("ERRO: " + alg.getNome() + " " + modo + " nao ordenou!");
            }
            tempos[a] = (fim - inicio) / 1_000_000.0;
            csv.printf("%s,%s,%d,%d,%s,%d,%.4f%n", alg.getNome(), modo, threads, tamanho, tipo, a + 1, tempos[a]);
        }
        csv.flush();
        return tempos;
    }

    /** Roda cada algoritmo algumas vezes sem medir, para o JIT compilar o codigo antes dos testes. */
    static void aquecerJvm(Ordenador[] algoritmos) throws Exception {
        System.out.println("Aquecendo a JVM...");
        for (int i = 0; i < 3; i++) {
            for (Ordenador alg : algoritmos) {
                int[] v1 = GeradorDados.gerar(GeradorDados.ALEATORIO, 10000);
                alg.ordenarSerial(v1);
                int[] v2 = GeradorDados.gerar(GeradorDados.ALEATORIO, 10000);
                alg.ordenarParalelo(v2, 2);
            }
        }
    }

    static void gravarResumo(PrintWriter resumo, String nome, String modo, int threads, int tamanho,
                             String tipo, double[] tempos, double speedup) {
        double min = tempos[0];
        double max = tempos[0];
        for (double t : tempos) {
            min = Math.min(min, t);
            max = Math.max(max, t);
        }
        resumo.printf("%s,%s,%d,%d,%s,%.4f,%.4f,%.4f,%.4f,%.3f%n",
                nome, modo, threads, tamanho, tipo, media(tempos), desvio(tempos), min, max, speedup);
        resumo.flush();
    }

    static double media(double[] v) {
        double soma = 0;
        for (double x : v) {
            soma += x;
        }
        return soma / v.length;
    }

    static double desvio(double[] v) {
        double m = media(v);
        double soma = 0;
        for (double x : v) {
            soma += (x - m) * (x - m);
        }
        return Math.sqrt(soma / (v.length - 1)); // desvio padrao amostral
    }
}
