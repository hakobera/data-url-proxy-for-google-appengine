DATA URL Proxy for Google AppEngine
===================================

HTML5 の Canvas は drawImage メソッドを利用することで、画像を表示することができます。
ただし、外部ドメインの画像を描画した場合、same-origin-policy 制約により、
描画コンテキストのピクセルデータにアクセスできません。（getImageData メソッドを呼び出すと、セキュリティエラー例外が発生します。）

DATA URL Proxy を利用することで、この制約を回避することができます。

処理
---------------

処理の大まかな流れは以下の通りです。

1. 画像データを Proxy Server 側で取得する
2. DATA URL 形式に変換し、テキストデータとして返す
3. 呼び出し側は、戻ってきたテキストデータを Image オブジェクトの src プロパティに設定

使い方
-----

以下は jQuery を利用した場合の例です。

	<canvas id="canvas"></canvas>

	var proxy = 'http://yourappid.appspot.com/dataurl/';
	var imageUrl = "画像のURL";
	$.ajax({
	  url: proxy,
	  type: 'GET',
	  data: { url: imageUrl },
	  dataType: 'text',
	  success: function(data) {
	    var img = new Image();
	    img.onload = function() {
	      var canvas = document.getElementById('canvas');
	      var ctx = canvas.getContext('2d');
	      ctx.drawImage(img, 0, 0, img.width, img.height);
	    };
	    img.src = data;
	  }
	});
