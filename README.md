# reversi-java

## 現在実装中のCPU

|Level|tactics|
|:-:|:--|
| 1 | 左上(列)優先の戦略 |
| 2 | 配置できるマス目の中からランダムに選ぶ戦略 |
| 3 | ひっくり返せる石が最も多いマス目を選ぶ戦略 |
| 4 | 盤面の特性を考慮してマス目を選ぶ戦略<br>優先度 角→端→ひっくり返せる石が最も多いマス目|