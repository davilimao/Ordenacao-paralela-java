# Ordenação serial x paralela em Java

Trabalho de Programação Concorrente e Paralela - Unifor.

Algoritmos: Bubble Sort (Odd-Even Transposition no paralelo), Quick Sort, Merge Sort e Counting Sort.

## Como executar

Precisa do JDK 17 ou superior.

```
javac -d bin src/*.java
java -cp bin Main
```

Também é possível rodar só alguns algoritmos: `java -cp bin Main QuickSort MergeSort`

Os resultados são gravados em `resultados/resultados.csv` (todas as amostras) e
`resultados/resumo.csv` (média, desvio padrão e speedup). Se os arquivos já existirem,
os novos resultados são adicionados no final, então apague a pasta `resultados` antes de uma nova rodada completa.

## Gráficos

```
pip install pandas matplotlib
python graficos.py
```
