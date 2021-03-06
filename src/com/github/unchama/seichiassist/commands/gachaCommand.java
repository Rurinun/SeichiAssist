package com.github.unchama.seichiassist.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.data.GachaData;
import com.github.unchama.seichiassist.data.MineStackGachaData;
import com.github.unchama.seichiassist.util.Util;

public class gachaCommand implements TabExecutor{
	public SeichiAssist plugin;
	Sql sql = SeichiAssist.plugin.sql;


	public gachaCommand(SeichiAssist plugin){
		this.plugin = plugin;
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command,
			String label, String[] args) {
		return null;
	}
	// /gacha set 0.01 (現在手にもってるアイテムが確率0.01でガチャに出現するように設定）
	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
			String label, String[] args) {

		if(args.length == 0){
			return false;
		}else if(args[0].equalsIgnoreCase("help")){

			sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD +"[コマンドリファレンス]");
			sender.sendMessage(ChatColor.RED + "/gacha mente");
			sender.sendMessage("メンテモードのON,OFF切り替え。ONだとガチャが引けなくなる");
			sender.sendMessage(ChatColor.RED + "/gacha give <all/プレイヤー名> <個数>");
			sender.sendMessage("ガチャ券配布コマンドです。allを指定で全員に配布(マルチ鯖対応済)");
			sender.sendMessage(ChatColor.RED + "/gacha vote <プレイヤー名>");
			sender.sendMessage("投票特典配布用コマンドです(マルチ鯖対応済)");
			sender.sendMessage(ChatColor.RED + "/gacha donate <プレイヤー名> <ポイント数>");
			sender.sendMessage("寄付者用プレミアムエフェクトポイント配布コマンドです(マルチ鯖対応済)");
			sender.sendMessage(ChatColor.RED + "/gacha get <ID> (<名前>)");
			sender.sendMessage("指定したガチャリストのIDを入手 (所有者付きにもできます) IDを0に指定するとガチャリンゴを入手できます");
			sender.sendMessage(ChatColor.RED + "/gacha add <確率>");
			sender.sendMessage("現在のメインハンドをガチャリストに追加。確率は1.0までで指定");
			sender.sendMessage(ChatColor.RED + "/gacha addms2 <確率> <名前> <レベル>");
			sender.sendMessage("現在のメインハンドをMineStack用ガチャリストに追加。確率は1.0までで指定");
			sender.sendMessage(ChatColor.RED + "/gacha addms <名前> <レベル> <ID>");
			sender.sendMessage("指定したガチャリストのIDを指定した名前とレベル(実際のレベルではないことに注意)でMineStack用ガチャリストに追加");
			sender.sendMessage(ChatColor.DARK_GRAY + "※ゲーム内でのみ実行できます");
			sender.sendMessage(ChatColor.RED + "/gacha list");
			sender.sendMessage("現在のガチャリストを表示");
			sender.sendMessage(ChatColor.RED + "/gacha listms");
			sender.sendMessage("現在のMineStack用ガチャリストを表示");
			sender.sendMessage(ChatColor.RED + "/gacha remove <番号>");
			sender.sendMessage("リスト該当番号のガチャ景品を削除");
			sender.sendMessage(ChatColor.RED + "/gacha removems");
			sender.sendMessage("リスト一番下のMineStackガチャ景品を削除(追加失敗した場合の修正用)");
			sender.sendMessage(ChatColor.RED + "/gacha setamount <番号> <個数>");
			sender.sendMessage("リスト該当番号のガチャ景品の個数変更。64まで");
			sender.sendMessage(ChatColor.RED + "/gacha setprob <番号> <確率>");
			sender.sendMessage("リスト該当番号のガチャ景品の確率変更");
			sender.sendMessage(ChatColor.RED + "/gacha move <番号> <移動先番号>");
			sender.sendMessage("リスト該当番号のガチャ景品の並び替えを行う");
			sender.sendMessage(ChatColor.RED + "/gacha clear");
			sender.sendMessage("ガチャリストを全消去する。取扱注意");
			sender.sendMessage(ChatColor.RED + "/gacha save");
			sender.sendMessage("コマンドによるガチャリストへの変更をmysqlに送信");
			sender.sendMessage(ChatColor.RED + "/gacha savems");
			sender.sendMessage("コマンドによるMineStack用ガチャリストへの変更をmysqlに送信");
			sender.sendMessage(ChatColor.DARK_RED + "※変更したら必ずsaveコマンドを実行(セーブされません)");
			sender.sendMessage(ChatColor.RED + "/gacha reload");
			sender.sendMessage("ガチャリストをmysqlから読み込む");
			sender.sendMessage(ChatColor.DARK_GRAY + "※onEnable時と同じ処理");
			sender.sendMessage(ChatColor.RED + "/gacha demo <回数>");
			sender.sendMessage("現在のガチャリストで指定回数試行し結果を表示。100万回まで");

			return true;

		}else if(args[0].equalsIgnoreCase("give")){
			//gacha give と入力したとき
			//[2]:プレイヤー名/all
			//[3]:個数
			if(args.length != 3){
				//引数が3でない時の処理
				sender.sendMessage(ChatColor.RED + "/gacha give <all/プレイヤー名> <個数>");
				sender.sendMessage("ガチャ券配布コマンドです。allを指定すると全員に配布します");
				return true;
			}else{
				//引数が3の時の処理

				//プレイヤー名を取得
				String name = Util.getName(args[1]);
				//個数取得
				int num = Util.toInt(args[2]);

				if(!name.equalsIgnoreCase("all")){
					//プレイヤー名がallでない時の処理

					//プレイヤーオンライン、オフラインにかかわらずsqlに送信(マルチ鯖におけるコンフリクト防止の為)
					sender.sendMessage(ChatColor.YELLOW + name + "のガチャ券配布処理開始…");

					//mysqlにも書き込んどく
					if(!sql.addPlayerBug(name,num)){
						sender.sendMessage(ChatColor.RED + "失敗");
					}else{
						sender.sendMessage(ChatColor.GREEN + "ガチャ券" + num +"枚加算成功");
					}
					return true;

				}else{
					//プレイヤー名がallの時の処理(全員に配布)

					//プレイヤーオンライン、オフラインにかかわらずsqlに送信(マルチ鯖におけるコンフリクト防止の為)
					sender.sendMessage(ChatColor.YELLOW + "全プレイヤーへのガチャ券配布処理開始…");

					//MySql処理
					if(!sql.addAllPlayerBug(num)){
						sender.sendMessage(ChatColor.RED + "失敗");
					}else{
						sender.sendMessage(ChatColor.GREEN + "ガチャ券" + num +"枚加算成功");
					}
					//addSorryForBug(sender,Util.toInt(args[2]));
					return true;
				}
			}


		}else if(args[0].equalsIgnoreCase("vote")){
			if(args.length != 2){
				//引数が2でない時の処理
				sender.sendMessage(ChatColor.RED + "/gacha vote <プレイヤー名>");
				sender.sendMessage("投票特典配布用コマンドです");
				return true;
			}else{
				//引数が2の時の処理

				//プレイヤー名を取得(小文字にする)
				String name = Util.getName(args[1]);

				//プレイヤーオンライン、オフラインにかかわらずsqlに送信(マルチ鯖におけるコンフリクト防止の為)
				sender.sendMessage(ChatColor.YELLOW + name + "の投票特典配布処理開始…");

				//mysqlにも書き込んどく
				if(!sql.addVotePoint(name)){
					sender.sendMessage(ChatColor.RED + "失敗");
				}else{
					sender.sendMessage(ChatColor.GREEN + "成功");
				}
				return true;
			}

		}else if(args[0].equalsIgnoreCase("donate")){
			if(args.length != 3){
				//引数が3でない時の処理
				sender.sendMessage(ChatColor.RED + "/gacha donate <プレイヤー名> <ポイント数>");
				sender.sendMessage("寄付者用プレミアムエフェクトポイント配布コマンドです");
				return true;
			}else{
				//引数が3の時の処理

				//プレイヤー名を取得(小文字にする)
				String name = Util.getName(args[1]);
				//配布ポイント数取得
				int num = Util.toInt(args[2]);

				//プレイヤーオンライン時はplayerdataに直接反映、オフライン時はsqlに送信(結果をsenderへ)
				sender.sendMessage(ChatColor.YELLOW + name + "のプレミアムエフェクトポイント配布処理開始…");

				//mysqlにも書き込んどく
				if(!sql.addPremiumEffectPoint(name,num) || !sql.addDonate(name, num)){
					sender.sendMessage(ChatColor.RED + "失敗");
				}else{
					sender.sendMessage(ChatColor.GREEN + "成功");
				}
				return true;
			}

		}else if(args[0].equalsIgnoreCase("mente")){
				//menteフラグ反転処理
				SeichiAssist.gachamente = !SeichiAssist.gachamente;
				if (SeichiAssist.gachamente){
					sender.sendMessage(ChatColor.GREEN + "ガチャシステムを一時停止しました");
				}else{
					sender.sendMessage(ChatColor.GREEN + "ガチャシステムを再開しました");
				}
				return true;
		}else if(args[0].equalsIgnoreCase("reload")){
			//gacha reload と入力したとき
			if(!sql.loadGachaData()){
				sender.sendMessage("mysqlからガチャデータのロードできませんでした");
			}else{
				sender.sendMessage("mysqlからガチャデータをロードしました");
			}
			return true;
		}else if(args[0].equalsIgnoreCase("loadfromyml")){
			//config.ymlから読み込む(mysql移行用コマンド)
			SeichiAssist.gachadatalist.clear();
			SeichiAssist.config.loadGachaData();
			sender.sendMessage("config.ymlからガチャデータをロードしました");
			sender.sendMessage("/gacha saveでmysqlに保存してください");
			return true;

		}else if(args[0].equalsIgnoreCase("save")){
			//gacha save と入力したとき
			if(!sql.saveGachaData()){
				sender.sendMessage("mysqlにガチャデータを保存できませんでした");
			}else{
				sender.sendMessage("mysqlにガチャデータを保存しました");
			}
			return true;

		}else if(args[0].equalsIgnoreCase("savems")){
			//gacha save と入力したとき
			if(!sql.saveMineStackGachaData()){
				sender.sendMessage("mysqlにMineStack用ガチャデータを保存できませんでした");
			}else{
				sender.sendMessage("mysqlにMineStack用ガチャデータを保存しました");
			}
			return true;

		}

		else if(args[0].equalsIgnoreCase("get")){
			if(args.length != 2 && args.length != 3 ){
				sender.sendMessage("/gacha get 1  または /gacha get 1 unchama のように、入手したいガチャアイテムのID(と名前)を入力してください");
				return true;
			}
			/*
			 * コンソールからのコマンドは処理しない - ここから
			 */
			if (!(sender instanceof Player)) {
				sender.sendMessage("このコマンドはゲーム内から実行してください");
				return true;
			}
			Player player = (Player) sender;
			/*
			 * ここまで
			 */

			if(args.length==2){
				int id = Util.toInt(args[1]);
				Gachaget(player,id,null);
			} else if(args.length==3){
				int id = Util.toInt(args[1]);
				Gachaget(player,id,args[2]);
			}
			return true;
		}

		else if(args[0].equalsIgnoreCase("add")){
			if(args.length != 2){
				sender.sendMessage("/gacha add 0.05  のように、追加したいアイテムの出現確率を入力してください");
				return true;
			}
			/*
			 * コンソールからのコマンドは処理しない - ここから
			 */
			if (!(sender instanceof Player)) {
				sender.sendMessage("このコマンドはゲーム内から実行してください");
				return true;
			}
			Player player = (Player) sender;
			/*
			 * ここまで
			 */

			double probability = Util.toDouble(args[1]);
			Gachaadd(player,probability);
			return true;
		}else if(args[0].equalsIgnoreCase("addms2")){
			if(args.length != 4){
				sender.sendMessage("/gacha addms2 0.05 gacha1 5  のように、追加したいアイテムの出現確率、名前、レベルを入力してください");
				return true;
			}
			/*
			 * コンソールからのコマンドは処理しない - ここから
			 */
			if (!(sender instanceof Player)) {
				sender.sendMessage("このコマンドはゲーム内から実行してください");
				return true;
			}
			Player player = (Player) sender;
			/*
			 * ここまで
			 */

			double probability = Util.toDouble(args[1]);
			int level = Util.toInt(args[3]);
			Gachaaddms2(player,probability, args[2], level);
			return true;
		}else if(args[0].equalsIgnoreCase("addms")){
			if(args.length != 3){
				sender.sendMessage("/gacha addms <名前> <ID>  のように、追加したいアイテムの名前とIDを入力してください");
				return true;
			}

			int level = Util.toInt(args[2]);
			int num = Util.toInt(args[3]);
			Gachaaddms(sender, args[1],level,num);

			return true;
		}else if(args[0].equalsIgnoreCase("remove")){
			if(args.length != 2){
				sender.sendMessage("/gacha remove 2 のように、削除したいリスト番号を入力してください");
				return true;
			}
			int num = Util.toInt(args[1]);
			Gacharemove(sender,num);
			return true;
		}else if(args[0].equalsIgnoreCase("removems")){
			if(args.length != 1){
				sender.sendMessage("/gacha removemsのように入力してください");
				return true;
			}
			Gacharemovems(sender);
			return true;
		}else if(args[0].equalsIgnoreCase("setamount")){
			if(args.length != 3){
				sender.sendMessage("/gacha setamount 2 1 のように、変更したいリスト番号と変更後のアイテム個数を入力してください");
				return true;
			}
			int num = Util.toInt(args[1]);
			int amount = Util.toInt(args[2]);
			GachaEditAmount(sender,num,amount);
			return true;
		}else if(args[0].equalsIgnoreCase("setprob")){
			if(args.length != 3){
				sender.sendMessage("/gacha setprob 2 1 のように、変更したいリスト番号と変更後の確率を入力してください");
				return true;
			}
			int num = Util.toInt(args[1]);
			double probability = Util.toDouble(args[2]);
			GachaEditProbability(sender,num,probability);
			return true;
		}else if(args[0].equalsIgnoreCase("move")){
			if(args.length != 3){
				sender.sendMessage("/gacha move 2 10 のように、変更したいリスト番号と変更後のリスト番号を入力してください");
				return true;
			}
			int num = Util.toInt(args[1]);
			int tonum = Util.toInt(args[2]);
			GachaMove(sender,num,tonum);
			return true;
		}else if(args[0].equalsIgnoreCase("list")){
			if(args.length != 1){
				sender.sendMessage("/gacha list で現在登録されているガチャアイテムを全て表示します");
			}
			if(SeichiAssist.gachadatalist.isEmpty()){
				sender.sendMessage("ガチャが設定されていません");
				return true;
			}
			Gachalist(sender);
			return true;
		} else if(args[0].equalsIgnoreCase("listms")){
			if(args.length != 1){
				sender.sendMessage("/gacha listms で現在登録されているガチャアイテムを全て表示します");
			}
			if(SeichiAssist.msgachadatalist.isEmpty()){
				sender.sendMessage("MineStack用ガチャリストが設定されていません");
				return true;
			}
			Gachalistms(sender);
			return true;
		} else if(args[0].equalsIgnoreCase("clear")){
			if(args.length != 1){
				sender.sendMessage("/gacha clear で現在登録されているガチャアイテムを削除します");
			}
			Gachaclear(sender);
			return true;
		}else if (args[0].equalsIgnoreCase("demo")){
			if(args.length != 2){
				sender.sendMessage("/gacha demo 10000  のように、試行したい回数を入力して下さい");
				return true;
			}
			int n = Util.toInt(args[1]);
			if(n > 1000000){
				sender.sendMessage("100万回以上は指定出来ません");
				return true;
			}
			//ガチャ券をn回試行してみる処理
			int i = 0;
			double p = 0.0;
			int gigantic = 0;
			int big = 0;
			int regular = 0;
			int potato = 0;
			while(n > i){
				p = runGachaDemo();
				if(p < 0.001){
					gigantic ++;
				}else if(p < 0.01){
					big ++;
				}else if(p < 0.1){
					regular ++;
				}else{
					potato ++;
				}
				i++;
			}
			sender.sendMessage(
					ChatColor.AQUA + "" + ChatColor.BOLD + "ガチャ券" + i + "回試行結果\n"
					+ ChatColor.RESET + "ギガンティック："+ gigantic +"回("+ ((double)gigantic/(double)i*100.0) +"%)\n"
					+ "大当たり："+ big +"回("+ ((double)big/(double)i*100.0) +"%)\n"
					+ "当たり："+ regular +"回("+ ((double)regular/(double)i*100.0) +"%)\n"
					+ "ハズレ："+ potato +"回("+ ((double)potato/(double)i*100.0) +"%)\n"
					);
			return true;
		}

		return false;
	}

	private void Gachaget(Player player,int _id, String name) {

		int id = _id-1;
		if(id>=-1 && id<SeichiAssist.gachadatalist.size()){
			//プレゼント用ガチャデータ作成
			GachaData present;
			//ガチャ実行
			if(id>=0){
				present = new GachaData(SeichiAssist.gachadatalist.get(id));
			} else {
				present = new GachaData(Util.getGachaimo(),1.0,1);
			}
			if(present.probability < 0.1){
				if(name!=null){
					present.addname(name);
				}
			}
			//ガチャデータのitemstackの数を再設定（バグのため）
			present.itemstack.setAmount(present.amount);
			//メッセージ設定
			String str = "";

			//プレゼントを格納orドロップ
			if(!Util.isPlayerInventryFill(player)){
				Util.addItem(player,present.itemstack);
			}else{
				Util.dropItem(player,present.itemstack);
				str += ChatColor.AQUA + "ガチャアイテムがドロップしました。";
			}
		}
	}


	private void Gachaadd(Player player,double probability) {
		GachaData gachadata = new GachaData();
		PlayerInventory inventory = player.getInventory();
		gachadata.itemstack = inventory.getItemInMainHand();
		gachadata.amount = inventory.getItemInMainHand().getAmount();
		gachadata.probability = probability;

		SeichiAssist.gachadatalist.add(gachadata);
		player.sendMessage(gachadata.itemstack.getType().toString() + "/" + gachadata.itemstack.getItemMeta().getDisplayName() + ChatColor.RESET + gachadata.amount + "個を確率" + gachadata.probability + "としてガチャに追加しました");
		player.sendMessage("/gacha saveでmysqlに保存してください");
	}

	private void Gachaaddms(CommandSender sender, String s, int level, int num) {
		int temp = num-1;
		if(temp>=0 && temp<SeichiAssist.gachadatalist.size()){
			GachaData g = SeichiAssist.gachadatalist.get(temp);
			MineStackGachaData mg = new MineStackGachaData();
			mg.amount = g.amount;
			mg.itemstack = g.itemstack;
			mg.probability = g.probability;
			mg.level = level;
			mg.obj_name = s;
			SeichiAssist.msgachadatalist.add(mg);
			sender.sendMessage("データガチャリストID" + num + "のデータを" + "変数名:" + s + ",レベル:" + level + "でMineStack用ガチャデータリストに追加しました");
			sender.sendMessage("/gacha savemsでmysqlに保存してください");
		}
		sender.sendMessage("正しくないIDです");
	}

	private void Gachaaddms2(Player player,double probability, String name, int level) {
		MineStackGachaData gachadata = new MineStackGachaData();
		PlayerInventory inventory = player.getInventory();
		gachadata.itemstack = inventory.getItemInMainHand();
		gachadata.amount = inventory.getItemInMainHand().getAmount();
		gachadata.probability = probability;
		gachadata.obj_name = name;
		gachadata.level = level;

		SeichiAssist.msgachadatalist.add(gachadata);
		player.sendMessage(gachadata.itemstack.getType().toString() + "/" + gachadata.itemstack.getItemMeta().getDisplayName() + ChatColor.RESET + gachadata.amount + "個を確率" + gachadata.probability + "としてMineStack用ガチャリストに追加しました");
		player.sendMessage("/gacha savemsでmysqlに保存してください");
	}

	private void Gachalist(CommandSender sender){
		int i = 1;
		double totalprobability = 0.0;
		sender.sendMessage(ChatColor.RED + "アイテム番号|アイテム名|アイテム数|出現確率");
		for (GachaData gachadata : SeichiAssist.gachadatalist) {
			sender.sendMessage(i + "|" + gachadata.itemstack.getType().toString() + "/" + gachadata.itemstack.getItemMeta().getDisplayName() + ChatColor.RESET + "|" + gachadata.amount + "|" + gachadata.probability + "(" + (gachadata.probability*100) + "%)");
			totalprobability += gachadata.probability;
			i++;
		}
		sender.sendMessage(ChatColor.RED + "合計確率:" + totalprobability + "(" + (totalprobability*100) + "%)");
		sender.sendMessage(ChatColor.RED + "合計確率は100%以内に収まるようにしてください");
	}
	private void Gachalistms(CommandSender sender){
		int i = 1;
		double totalprobability = 0.0;
		sender.sendMessage(ChatColor.RED + "アイテム番号|レベル|変数名|アイテム名|アイテム数|出現確率");
		for (MineStackGachaData gachadata : SeichiAssist.msgachadatalist) {
			sender.sendMessage(i + "|" + gachadata.level + "|" + gachadata.obj_name + "|" + gachadata.itemstack.getType().toString() + "/" + gachadata.itemstack.getItemMeta().getDisplayName() + ChatColor.RESET + "|" + gachadata.amount + "|" + gachadata.probability + "(" + (gachadata.probability*100) + "%)");
			//totalprobability += gachadata.probability;
			i++;
		}
		//sender.sendMessage(ChatColor.RED + "合計確率:" + totalprobability + "(" + (totalprobability*100) + "%)");
		//sender.sendMessage(ChatColor.RED + "合計確率は100%以内に収まるようにしてください");
	}
	private void Gacharemove(CommandSender sender,int num) {
		if(num < 1 || SeichiAssist.gachadatalist.size() < num){
			sender.sendMessage("アイテム番号が間違っているようです");
			return;
		}
		GachaData gachadata = SeichiAssist.gachadatalist.get(num-1);
		SeichiAssist.gachadatalist.remove(num-1);
		sender.sendMessage(num + "|" + gachadata.itemstack.getType().toString() + "/" + gachadata.itemstack.getItemMeta().getDisplayName() + ChatColor.RESET + "|" + gachadata.amount + "|" + gachadata.probability + "を削除しました");
		sender.sendMessage("/gacha saveでmysqlに保存してください");
	}
	private void Gacharemovems(CommandSender sender) {

		if(SeichiAssist.msgachadatalist.size() == 0){
			sender.sendMessage("MineStack用ガチャデータリストが空です");
			return;
		}
		int size = SeichiAssist.msgachadatalist.size();
		MineStackGachaData mg = SeichiAssist.msgachadatalist.get(size-1);
		SeichiAssist.msgachadatalist.remove(size-1);
		sender.sendMessage(size + "|" + mg.level + "|" + mg.obj_name + "|" + mg.itemstack.getType().toString() + "/" + mg.itemstack.getItemMeta().getDisplayName() + ChatColor.RESET + "|" + mg.amount + "|" + mg.probability + "を削除しました");
		sender.sendMessage("/gacha savemsでmysqlに保存してください");
	}
	private void GachaEditAmount(CommandSender sender,int num,int amount) {
		if(num < 1 || SeichiAssist.gachadatalist.size() < num){
			sender.sendMessage("アイテム番号が間違っているようです");
			return;
		}
		GachaData gachadata = SeichiAssist.gachadatalist.get(num-1);
		gachadata.amount = amount;
		SeichiAssist.gachadatalist.set(num-1,gachadata);
		sender.sendMessage(num + "|" + gachadata.itemstack.getType().toString() + "/" + gachadata.itemstack.getItemMeta().getDisplayName() + ChatColor.RESET + "のアイテム数を" + gachadata.amount + "個に変更しました");
	}
	private void GachaEditProbability(CommandSender sender,int num,double probability) {
		if(num < 1 || SeichiAssist.gachadatalist.size() < num){
			sender.sendMessage("アイテム番号が間違っているようです");
			return;
		}
		GachaData gachadata = SeichiAssist.gachadatalist.get(num-1);
		gachadata.probability = probability;
		SeichiAssist.gachadatalist.set(num-1,gachadata);
		sender.sendMessage(num + "|" + gachadata.itemstack.getType().toString() + "/" + gachadata.itemstack.getItemMeta().getDisplayName() + ChatColor.RESET + "の確率を" + gachadata.probability + "個に変更しました");
		sender.sendMessage("/gacha saveでmysqlに保存してください");
	}
	private void GachaMove(CommandSender sender,int num,int tonum) {
		if(num < 1 || SeichiAssist.gachadatalist.size() < num){
			sender.sendMessage("アイテム番号が間違っているようです");
			return;
		}
		if(tonum < 1 || SeichiAssist.gachadatalist.size() < tonum){
			sender.sendMessage("アイテム番号が間違っているようです");
			return;
		}
		GachaData gachadata = SeichiAssist.gachadatalist.get(num-1);
		SeichiAssist.gachadatalist.remove(num-1);
		SeichiAssist.gachadatalist.add(tonum-1,gachadata);
		sender.sendMessage(num + "|" + gachadata.itemstack.getType().toString() + "/" + gachadata.itemstack.getItemMeta().getDisplayName() + ChatColor.RESET + "をリスト番号" + tonum + "番に移動しました");
		sender.sendMessage("/gacha saveでmysqlに保存してください");
	}
	private void Gachaclear(CommandSender sender) {
		SeichiAssist.gachadatalist.clear();
		sender.sendMessage("すべて削除しました");
		sender.sendMessage("/gacha saveを実行するとmysqlのデータも全削除されます");
		sender.sendMessage("削除を取り消すには/gacha reloadコマンドを実行します");
	}
	private double runGachaDemo() {
		double sum = 1.0;
		double rand = 0.0;

		rand = Math.random();

		for (GachaData gachadata : SeichiAssist.gachadatalist) {
		    sum -= gachadata.probability;
		    if (sum <= rand) {
                return gachadata.probability;
            }
		}
		return 1.0;
	}
	/*
	private void addSorryForBug(CommandSender sender,int num) {
		for(PlayerData playerdata : SeichiAssist.playermap.values()){
			//オンラインプレイヤーに対しての追加処理
			//プレイヤーがオフラインの時処理を終了、次のプレイヤーへ
			if(playerdata.isOffline()){
				if(SeichiAssist.DEBUG){
					Util.sendEveryMessage(playerdata.name + "は不在により処理中止");
				}
				continue;
			}
			//プレイヤー型を取得
			Player player = plugin.getServer().getPlayer(playerdata.uuid);
			playerdata.numofsorryforbug += num;
			//プレイヤーにお知らせしちゃう
			playerdata.NotifySorryForBug(player);
			sender.sendMessage(ChatColor.LIGHT_PURPLE + "" + num +"個のガチャ券をお詫びとして" + playerdata.name + "のデータに更新しました");
		}
		//MySqlの値も処理
		if(!sql.addAllPlayerBug(num)){
			sender.sendMessage("mysqlへのガチャの加算に失敗しました");
		}else{
			sender.sendMessage("mysqlに保存されている全プレイヤーへガチャ券" + num +"枚を加算しました");
		}
	}
	*/

}
