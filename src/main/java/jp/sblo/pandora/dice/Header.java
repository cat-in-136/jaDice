/**
 *
 */
package jp.sblo.pandora.dice;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * @author Jiro
 *
 */
final class  Header {
	private final	int	L_HEADERNAME =  100;    //   ヘッダー部文字列長
	private final 	int	L_DICTITLE = 40;	    //   辞書タイトル名長

	public String headername; //   辞書ヘッダータイトル
	public String dictitle;   //   辞書名
	public short version;      //   辞書のバージョン
	public short lword;        //   見出語の最大長
	public short ljapa;        //   訳語の最大長
	public short block_size;      //  (256 ) １ブロックのバイト数 固定
	public short index_block;     //   インデックスブロック数
	public short header_size;     //   ヘッダーのバイト数
	public short index_size;  //  ( ) インデックスのバイト数 未使用

	public short empty_block;     // 空きブロックの先頭物理ブロック番号（ないときは-1  ）
	public short nindex;       //  ( ) インデックスの要素の数 未使用
	public short nblock;       //  ( ) 使用データブロック数 未使用
	public int   nword;    //   登録単語数

	public byte dicorder;      //   辞書の順番
	public byte dictype;       //   辞書の種別

	public byte attrlen;       //   単語属性の長さ
	public byte os;          // OS
	public int  olenumber;      // OLE  用シリアル番号
	public short lid_word;      //  ID 見出語言語

	public short lid_japa;      //  ID 訳語部言語
	public short lid_exp;      //  ID 用例部言語
	public short lid_pron;      //  ID 発音記号言語
	public short lid_other;      //  ID その他言語
	public boolean index_blkbit;   // false:16bit, true:32bit
	public int extheader;      //   拡張ヘッダーサイズ
	public int empty_block2;     //   空きブロック先頭物理ブロック番号
	public int nindex2;       //   インデックス要素の数
	public int nblock2;       //   使用データブロック数

	public int update_count;    //   辞書更新回数
	public String dicident;      //   辞書識別子

	/**
	 * コンストラクタ
	 * @param header_block 辞書ファイルの先頭ブロック(256bytes)
	 */
	public Header(){
	}

	/**
	 * @param header_block ヘッダーデータ部分
	 *
	 * @return 辞書バージョン
	 */
	public int load(ByteBuffer header_block)
	{
		int ret = 0;
		Charset sjisset = Charset.forName("X-SJIS");

		byte[] headernamebuff = new byte [L_HEADERNAME];
		byte[] dictitlebuff = new byte [L_DICTITLE];

		try {
			header_block.flip();
			header_block.order(ByteOrder.LITTLE_ENDIAN);
			header_block.get(headernamebuff);
			headername = sjisset.decode(ByteBuffer.wrap(headernamebuff))
					.toString();
			header_block.get(dictitlebuff);
			dictitle = sjisset.decode(ByteBuffer.wrap(dictitlebuff)).toString();
			version = header_block.getShort();
			if ((version & 0xFF00) == 0x0500 || (version & 0xFF00) == 0x0600) {
				lword = header_block.getShort();
				ljapa = header_block.getShort();

				block_size = header_block.getShort();
				index_block = header_block.getShort();
				header_size = header_block.getShort();
				index_size = header_block.getShort();
				empty_block = header_block.getShort();
				nindex = header_block.getShort();
				nblock = header_block.getShort();

				nword = header_block.getInt();

				dicorder = header_block.get();
				dictype = header_block.get();
				attrlen = header_block.get();
				os = header_block.get();

				olenumber = header_block.getInt();
				lid_word = header_block.getShort();

				lid_japa = header_block.getShort();
				lid_exp = header_block.getShort();
				lid_pron = header_block.getShort();
				lid_other = header_block.getShort();
				index_blkbit = (header_block.get() != 0);
				header_block.get(); // dummy0
				extheader = header_block.getInt();
				empty_block2 = header_block.getInt();
				nindex2 = header_block.getInt();
				nblock2 = header_block.getInt();

				// 固定部分チェック
				if ( attrlen == 1 ){
					ret = version >> 8;
				}
			} else if ((version & 0xFF00) == 0x0400) {

				lword = header_block.getShort();
				ljapa = header_block.getShort();

				block_size = header_block.getShort();
				index_block = header_block.getShort();
				header_size = header_block.getShort();
				index_size = header_block.getShort();
				empty_block = header_block.getShort();
				nindex = header_block.getShort();
				nblock = header_block.getShort();

				nword = header_block.getInt();

				dicorder = header_block.get();
				dictype = header_block.get();
				attrlen = header_block.get();

				olenumber = header_block.getInt();
				os = header_block.get();

				lid_word = header_block.getShort();
				lid_japa = header_block.getShort();
				lid_exp = header_block.getShort();
				lid_pron = header_block.getShort();
				lid_other = header_block.getShort();
				extheader = header_block.getInt();
				empty_block2 = header_block.getInt();
				nindex2 = header_block.getInt();
				nblock2 = header_block.getInt();
				index_blkbit = (header_block.get() != 0);
				// 固定部分チェック
				if ( block_size == 0x100 &&
					header_size == 0x100 &&
					attrlen == 1 ){
					ret = version >> 8;
				}
			} else {
				// not support
			}
		} catch (BufferUnderflowException e) {
			// アンダーフロー

		}
		return ret;
	}

}
