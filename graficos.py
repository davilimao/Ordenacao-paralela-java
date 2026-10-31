# -*- coding: utf-8 -*-
# Gera os graficos a partir de resultados/resumo.csv
# Uso: python graficos.py   (precisa de pandas e matplotlib)
import pandas as pd
import matplotlib.pyplot as plt

df = pd.read_csv("resultados/resumo.csv")
algoritmos = ["BubbleSort", "QuickSort", "MergeSort", "CountingSort"]
cores = {1: "black", 2: "tab:blue", 4: "tab:orange", 8: "tab:green"}

# 1) Tempo medio x tamanho (entrada aleatoria), serial e paralelo, um grafico por algoritmo
fig, eixos = plt.subplots(2, 2, figsize=(11, 8))
for ax, alg in zip(eixos.flat, algoritmos):
    d = df[(df.algoritmo == alg) & (df.tipo_entrada == "aleatorio")]
    for t in [1, 2, 4, 8]:
        s = d[d.threads == t].sort_values("tamanho")
        rotulo = "Serial" if t == 1 else f"Paralelo {t} threads"
        ax.errorbar(s.tamanho, s.media_ms, yerr=s.desvio_ms, marker="o", capsize=3,
                    color=cores[t], label=rotulo)
    ax.set_title(alg)
    ax.set_xscale("log")
    ax.set_yscale("log")
    ax.set_xlabel("Tamanho do vetor (n)")
    ax.set_ylabel("Tempo médio (ms)")
    ax.grid(True, which="both", alpha=0.3)
    ax.legend(fontsize=8)
fig.suptitle("Tempo médio de execução - entrada aleatória")
fig.tight_layout()
fig.savefig("resultados/grafico1_tempo_por_tamanho.png", dpi=150)

# 2) Speedup x numero de threads (maior tamanho de cada algoritmo, entrada aleatoria)
fig, ax = plt.subplots(figsize=(7, 5))
for alg in algoritmos:
    d = df[(df.algoritmo == alg) & (df.tipo_entrada == "aleatorio")]
    nmax = d.tamanho.max()
    s = d[d.tamanho == nmax].sort_values("threads")
    ax.plot(s.threads, s.speedup, marker="o", label=f"{alg} (n={nmax:,})".replace(",", "."))
ax.axhline(1, color="gray", linestyle="--", linewidth=1)
ax.set_xticks([1, 2, 4, 8])
ax.set_xlabel("Número de threads (1 = serial)")
ax.set_ylabel("Speedup (tempo serial / tempo paralelo)")
ax.set_title("Speedup por número de threads - entrada aleatória")
ax.grid(True, alpha=0.3)
ax.legend()
fig.tight_layout()
fig.savefig("resultados/grafico2_speedup.png", dpi=150)

# 3) Influencia do tipo de entrada (serial, maior tamanho de cada algoritmo)
fig, ax = plt.subplots(figsize=(8, 5))
tipos = ["aleatorio", "ordenado", "inverso"]
largura = 0.25
for i, tipo in enumerate(tipos):
    valores = []
    for alg in algoritmos:
        d = df[(df.algoritmo == alg) & (df.threads == 1)]
        nmax = d.tamanho.max()
        valores.append(d[(d.tamanho == nmax) & (d.tipo_entrada == tipo)].media_ms.values[0])
    x = [j + (i - 1) * largura for j in range(len(algoritmos))]
    ax.bar(x, valores, width=largura, label={"aleatorio": "aleatório", "ordenado": "ordenado", "inverso": "inverso"}[tipo])
ax.set_xticks(range(len(algoritmos)))
ax.set_xticklabels([f"{a}\n(n = {m:,})".replace(",", ".") for a, m in zip(algoritmos, [50000, 1000000, 1000000, 1000000])])
ax.set_yscale("log")
ax.set_ylabel("Tempo médio serial (ms)")
ax.set_title("Influência do tipo de entrada (versão serial)")
ax.grid(True, axis="y", alpha=0.3)
ax.legend()
fig.tight_layout()
fig.savefig("resultados/grafico3_tipo_entrada.png", dpi=150)

print("Graficos salvos em resultados/")
