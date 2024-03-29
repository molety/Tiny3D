＝＝＝＝＝ くるくる3Dデモ ＝＝＝＝＝

★これは何？

auケータイのオープンアプリプレイヤー上で動作する、デモソフトです。
3Dの何かがくるくる回ります。

このアーカイブに含まれるバイナリを直接auケータイに転送、実行することはできません。
以下のURLで実行可能バイナリを公開していたのですが、既に公開終了しました。
    http://mole.skr.jp/ez/


★操作

↑キー：上を向く
↓キー：下を向く
←キー：左を向く
→キー：右を向く
 1キー：左に傾く
 3キー：右に傾く
 2キー：近づく
 8キー：遠ざかる
 0キー：元の位置に「うにょっ」と戻る

 *キー：1画面モード
 #キー：2画面モード


★ライセンス

本ソフトウェアは、MIT Licenseの下で公開しています。(NYSLから変更しました)
LICENSEファイルをお読みください。


★ソースについて

T3*.javaがライブラリ、Tiny3D*.javaがそれを使ったアプリケーション、
という想定で書いてあります。

Windows環境の場合、アーカイブ内のmakedoc.batを実行するとjavadocを
生成できます。その際、
　・javadocコマンドにパスが通っていること
　・Sun Java Wireless Toolkit 2.5.1 for CLDC(Windows用多言語版)を
　　デフォルトの位置にインストールしていること
を前提としています。環境が異なる場合は適宜調整してください。


★参考にしたもの

[書籍]
金谷 一朗「3D-CGプログラマーのための実践クォータニオン」、工学社、2004年、ISBN4-7775-1031-X
床井 浩平「GLUTによるOpenGL入門」、工学社、2005年、ISBN4-7775-1134-0
金谷 一朗「3D-CGプログラマーのためのクォータニオン入門」、工学社、2004年、ISBN4-7775-1016-6

[Web]
http://3dinc.blog45.fc2.com/blog-entry-392.html  (gluLookAt)
http://www.manpagez.com/man/3/glFrustum/  (glFrustum)
http://marupeke296.com/FBX_No7_UV.html  (ライティング)
http://marina.sys.wakayama-u.ac.jp/~tokoi/?date=20051007  (ライティング)
http://f4.aaa.livedoor.jp/~pointc/203/No.7397.htm  (非再帰版マージソート)
http://garugari.jugem.jp/?eid=433  (平方根の逆数の高速化)
http://niffy-you-nats.hp.infoseek.co.jp/iappli/LunaMath.java  (平方根の逆数の高速化、Java版)
http://www.yamagami-planning.com/soft/optimize/optimize01/  (三角関数のテーブル化)


★履歴

0.0.3 (2008/10/30)
- もうちょっと作り込んでからソース公開しようと思ってたけど、
  いつまで経っても公開できそうにないので、エイヤで出してみた。
  ちなみに0.0.3から始まっているのは、現在のMIDlet-Versionが
  0.0.3だから。今後も飛び飛びで上がっていく可能性あり。
  (いや、永久に上がらないかも?)

0.0.4 (2008/11/09)
- 見た目には特に変化なし。ソース上はいろいろ準備中。
- ProGuardを4.2に上げた。
- Mercurialのタグ付け機能を試してみるためにバージョンを上げた。

0.0.5 (2008/11/15)
- 面法線ベクトルの向きによるカリングを実装した。
  これでやっと正二十面体が破綻なく表示できるようになった。
- その他、視点の設定や透視投影なども実装。
- Zソートも実装したつもりだが、検証できるデータを用意していない。
- ライティングは作りかけで、まだ動いていない。
- クォータニオンから回転行列を作るところで、回転が逆向きになっている
  ようなので暫定対策した。一度ちゃんと計算式を追ってみる必要がある。

0.0.6 (2008/11/16)
- Zソートが動いていることを確認した。
- ベクトルの正規化を高速化。したつもりだが、速くなったかは確かめてない。
- x/y/z軸周りの回転行列を作るメソッドを追加した。
- クォータニオンから回転行列を作るところで、回転が逆向きになっている件の
  恒久対策をした。計算式はある程度までしか追えなかったが、まず大丈夫だろう。
- ライティングを実装した。今のところ、環境光と拡散反射光のみ。
  鏡面反射光は、後で実装するつもりだが、フラットシェーディングなので
  あまり効果がないかも。

0.0.7 (2008/11/17)
- 鏡面反射光を実装した。ただし、ここまでのライティングはすべて平行光源。
  点光源はまだ。
- 三角形以外の多角形に対応した。ただし、凸多角形であること。

0.0.8 (2008/11/18)
- 点光源を実装した。減衰率のシミュレートがこれでいいのか自信はないが、
  だいたいそれっぽく動いているようなので良しとする。
- z座標によるカリングを実装した。重心が投影面より近くにあるポリゴンを
  表示しなくする。遠方のポリゴンのカリングは実装していない。
- 画角と縦横比によって透視投影のウィンドウを設定できるAPIを作った。

0.0.9 (2008/11/19)
- sin,cosをテーブル化した。たぶん速くなっていると思う。
  いずれasin,acosが必要になるかもしれないので、そのための布石でもある。
  (CLDC1.1+MIDP2.0にはasin,acosが無い)
- roll,pitch,yawによる視点設定を実装した。
- 時間管理をする機構を作った。これで、実機とエミュレータとで、物体を同じ
  スピードで動かせるようになった。(フレームレートは負荷に応じて変わる)
- キー操作で視点位置を動かせるようにした。
- 見栄えを少し整えた。

0.1.0 (2008/11/22)
- アプリ名とアイコン、サイトのURLをちゃんと設定した。(やっと)
- カメラオブジェクトを複数作れるようにした。ただし、まだ中途半端なので
  使い道はあまりなさそう。
- javadocを生成するバッチファイルを同梱。

0.1.1 (2008/11/23)
- ある姿勢から別の姿勢への補間(interpolation)を実装した。
  これを使って、0キーでカメラ位置を元に戻せるようにしてみた。
  かなり力ずくで実装したので、動作は重かろうと思ったが、それほどでもないようだ。
- 操作説明を表示できるようにした。

0.1.2 (2008/11/24)
- 2画面モードを作った。2つの視点から物体を眺められる。
- 2画面モードがさすがに重いので、鏡面反射と点光源のシミュレートを
  デモから外した。
- 若干のリファクタリングをした。

1.0.0 (2008/11/29)
- コメントを整備した。(javadoc対応)
- クラス / メソッド / フィールドのアクセス権を設定した。
- 若干のリファクタリングをした。
- 区切りのため、バージョンを1.0.0とした。

1.0.1 (2008/12/09)
- マニュアルモード中、まったくスリープしていなかったのを修正した。
- マニュアルモード中も、フレームレートを表示し続けるようにした。
- 開発用エミュレータで実行時、電源キー押下で中断した後に再実行すると、
  画面が乱れていたのを修正した。(スレッドが複数走っているせいだった)
