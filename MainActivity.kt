package com.cocktaillab.app

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

private data class Ingredient(val name:String,val amount:String)
private data class Cocktail(val id:String,val name:String,val base:String,val method:String,val glass:String,val garnish:String,val ingredients:List<Ingredient>,val custom:Boolean=false)
private data class Bottle(val name:String,val totalMl:Double,val remainingMl:Double,val price:Double,val abv:Double)

private val classics=listOf(
 Cocktail("negroni","Negroni","Gin","Stir","Rocks","Orange peel",listOf(Ingredient("Gin","30 ml"),Ingredient("Campari","30 ml"),Ingredient("Sweet Vermouth","30 ml"))),
 Cocktail("old-fashioned","Old Fashioned","Whiskey","Stir","Rocks","Orange peel",listOf(Ingredient("Bourbon/Rye","60 ml"),Ingredient("Sugar syrup","7.5 ml"),Ingredient("Angostura bitters","2 dashes"))),
 Cocktail("daiquiri","Daiquiri","Rum","Shake","Coupe","Lime wheel",listOf(Ingredient("White Rum","60 ml"),Ingredient("Lime juice","25 ml"),Ingredient("Simple syrup","15 ml"))),
 Cocktail("margarita","Margarita","Tequila","Shake","Coupe","Lime wheel + salt rim",listOf(Ingredient("Tequila","50 ml"),Ingredient("Triple Sec","25 ml"),Ingredient("Lime juice","25 ml"))),
 Cocktail("martini","Dry Martini","Gin","Stir","Nick & Nora","Lemon twist or olive",listOf(Ingredient("Gin","60 ml"),Ingredient("Dry Vermouth","10 ml"),Ingredient("Orange bitters","1 dash"))),
 Cocktail("tom-collins","Tom Collins","Gin","Shake + top","Highball","Lemon + cherry",listOf(Ingredient("Gin","45 ml"),Ingredient("Lemon juice","30 ml"),Ingredient("Simple syrup","15 ml"),Ingredient("Soda","Top"))),
 Cocktail("whiskey-sour","Whiskey Sour","Whiskey","Shake","Rocks","Lemon + cherry",listOf(Ingredient("Bourbon","60 ml"),Ingredient("Lemon juice","30 ml"),Ingredient("Simple syrup","20 ml"),Ingredient("Egg white","20 ml"))),
 Cocktail("mojito","Mojito","Rum","Build + churn","Highball","Mint + lime",listOf(Ingredient("White Rum","50 ml"),Ingredient("Lime juice","25 ml"),Ingredient("Simple syrup","15 ml"),Ingredient("Soda","Top"))),
 Cocktail("cosmopolitan","Cosmopolitan","Vodka","Shake","Coupe","Orange twist",listOf(Ingredient("Vodka","40 ml"),Ingredient("Triple Sec","20 ml"),Ingredient("Cranberry juice","30 ml"),Ingredient("Lime juice","15 ml"))),
 Cocktail("moscow-mule","Moscow Mule","Vodka","Build","Mule mug","Lime wedge",listOf(Ingredient("Vodka","50 ml"),Ingredient("Lime juice","15 ml"),Ingredient("Ginger beer","100 ml")))
)

class MainActivity:ComponentActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);setContent{CocktailLab()}}}

private fun ingJson(i:Ingredient)=JSONObject().apply{put("name",i.name);put("amount",i.amount)}
private fun cocktailJson(c:Cocktail)=JSONObject().apply{put("id",c.id);put("name",c.name);put("base",c.base);put("method",c.method);put("glass",c.glass);put("garnish",c.garnish);put("custom",c.custom);put("ingredients",JSONArray().apply{c.ingredients.forEach{put(ingJson(it))}})}
private fun jsonCocktail(o:JSONObject):Cocktail{val a=o.optJSONArray("ingredients")?:JSONArray();val list=buildList{for(i in 0 until a.length()){val x=a.optJSONObject(i)?:continue;add(Ingredient(x.optString("name"),x.optString("amount")))}};val n=o.optString("name","Untitled");return Cocktail(o.optString("id",n.lowercase().replace(" ","-")),n,o.optString("base"),o.optString("method"),o.optString("glass"),o.optString("garnish"),list,o.optBoolean("custom",true))}
private fun bottleJson(b:Bottle)=JSONObject().apply{put("name",b.name);put("totalMl",b.totalMl);put("remainingMl",b.remainingMl);put("price",b.price);put("abv",b.abv)}
private fun jsonBottle(o:JSONObject)=Bottle(o.optString("name"),o.optDouble("totalMl"),o.optDouble("remainingMl"),o.optDouble("price"),o.optDouble("abv"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun CocktailLab(){
 val ctx=androidx.compose.ui.platform.LocalContext.current
 var customs by remember{mutableStateOf(loadCocktails(ctx))};var bottles by remember{mutableStateOf(loadBottles(ctx))};var favorites by remember{mutableStateOf(loadFavorites(ctx))}
 var tab by remember{mutableIntStateOf(0)};var query by remember{mutableStateOf("")};var selected by remember{mutableStateOf<Cocktail?>(null)};var showAdd by remember{mutableStateOf(false)};var showBottle by remember{mutableStateOf(false)}
 val all=classics+customs
 val openImport=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri:Uri?->if(uri!=null){val text=ctx.contentResolver.openInputStream(uri)?.bufferedReader()?.use{it.readText()};if(text!=null){val imported=runCatching{val a=JSONArray(text);buildList{for(i in 0 until a.length())add(jsonCocktail(a.getJSONObject(i)))}}.getOrElse{parseCsv(text)};customs=(customs+imported).distinctBy{it.name.lowercase()};saveCocktails(ctx,customs)}}}
 val export=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")){uri:Uri?->if(uri!=null)ctx.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use{it.write(JSONArray(customs.map{cocktailJson(it)}).toString(2))}}
 MaterialTheme(colorScheme=darkColorScheme(primary=Color(0xFFD7A65A),background=Color(0xFF12100F),surface=Color(0xFF1C1917))){
  Scaffold(topBar={TopAppBar(title={Text("Cocktail Lab",fontWeight=FontWeight.Bold)},actions={Text("🍸",Modifier.padding(16.dp))})},bottomBar={NavigationBar{NavigationBarItem(tab==0,{tab=0},icon={Text("🍸")},label={Text("酒谱")});NavigationBarItem(tab==1,{tab=1},icon={Text("⭐")},label={Text("收藏")});NavigationBarItem(tab==2,{tab=2},icon={Text("🧪")},label={Text("工具")})}}){pad->
   when{selected!=null->Detail(selected!!,bottles,{selected=null});tab==0->RecipeList(all,query,{query=it},{selected=it},favorites){favorites=toggleFav(favorites,it.id);saveFavorites(ctx,favorites)};tab==1->RecipeList(all.filter{favorites.contains(it.id)},query,{query=it},{selected=it},favorites){favorites=toggleFav(favorites,it.id);saveFavorites(ctx,favorites)};else->ToolsScreen(bottles,{bottles=it;saveBottles(ctx,it)},{showBottle=true},customs,all,openImport,export)}
  }}
 if(showAdd){} // reserved for future editor expansion
 if(showBottle){BottleDialog({showBottle=false},{b->bottles=(bottles+b);saveBottles(ctx,bottles);showBottle=false})}
}

@Composable private fun RecipeList(list:List<Cocktail>,query:String,onQuery:(String)->Unit,onOpen:(Cocktail)->Unit,favs:Set<String>,onFav:(Cocktail)->Unit){Column(Modifier.fillMaxSize().padding(16.dp)){Text("鸡尾酒酒谱",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Spacer(Modifier.height(8.dp));OutlinedTextField(query,onQuery,Modifier.fillMaxWidth(),label={Text("搜索酒名、基酒或材料")});Spacer(Modifier.height(8.dp));LazyColumn{items(list.filter{it.name.contains(query,true)||it.base.contains(query,true)||it.ingredients.any{x->x.name.contains(query,true)}}){c->Card(onClick={onOpen(c)},Modifier.fillMaxWidth().padding(vertical=4.dp)){Row(Modifier.padding(15.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(c.name,fontWeight=FontWeight.Bold);Text("${c.base} · ${c.method} · ${c.glass}")};TextButton({onFav(c)}){Text(if(favs.contains(c.id))"★" else "☆")}}}}}}}

@Composable private fun Detail(c:Cocktail,bottles:List<Bottle>,onBack:()->Unit){var servings by remember{mutableIntStateOf(1)};Column(Modifier.fillMaxSize().padding(18.dp)){TextButton(onBack){Text("‹ 返回")};Text(c.name,style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text("${c.base} · ${c.method}",Modifier.padding(vertical=6.dp));HorizontalDivider();Spacer(Modifier.height(10.dp));Text("材料 × $servings",style=MaterialTheme.typography.titleMedium);c.ingredients.forEach{Text("• ${it.name}  ${scaleAmount(it.amount,servings)}",Modifier.padding(vertical=3.dp))};Spacer(Modifier.height(8.dp));Text("杯型：${c.glass}");Text("装饰：${c.garnish}");Spacer(Modifier.height(12.dp));CostCard(c,bottles);Spacer(Modifier.height(10.dp));Row(verticalAlignment=Alignment.CenterVertically){Text("出杯：$servings");Spacer(Modifier.width(8.dp));Button({servings=(servings-1).coerceAtLeast(1)}){Text("−")};Spacer(Modifier.width(5.dp));Button({servings++}){Text("＋")}}}}

@Composable private fun CostCard(c:Cocktail,bottles:List<Bottle>){val cost=recipeCost(c,bottles);val abv=recipeAbv(c,bottles);Card{Column(Modifier.padding(14.dp)){Text("工具计算",fontWeight=FontWeight.Bold);Text(if(cost>=0)"估算单杯成本：¥${fmt(cost)}" else "成本：缺少库存价格");Text(if(abv>=0)"估算 ABV：${fmt(abv)}%" else "ABV：需要基酒酒精度");Text("库存匹配：${if(canMake(c,bottles))"可以制作" else "缺少材料或库存"}")}}}

@Composable private fun ToolsScreen(bottles:List<Bottle>,onBottles:(List<Bottle>)->Unit,onAdd:()->Unit,customs:List<Cocktail>,all:List<Cocktail>,openImport:()->Unit,export:()->Unit){var batch by remember{mutableIntStateOf(10)};Column(Modifier.fillMaxSize().padding(16.dp)){Text("酒吧工具",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Spacer(Modifier.height(10.dp));Row{Button(onAdd){Text("＋ 酒瓶库存")};Spacer(Modifier.width(8.dp));OutlinedButton(openImport){Text("导入配方")};Spacer(Modifier.width(8.dp));OutlinedButton(export){Text("导出我的配方")}};Spacer(Modifier.height(16.dp));Text("库存",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);LazyColumn(Modifier.heightIn(max=260.dp)){items(bottles){b->Card(Modifier.fillMaxWidth().padding(vertical=3.dp)){Column(Modifier.padding(12.dp)){Text(b.name,fontWeight=FontWeight.Bold);Text("剩余 ${fmt(b.remainingMl)} / ${fmt(b.totalMl)} ml · ${fmt(b.abv)}% ABV · ¥${fmt(b.price)}")}}}};Spacer(Modifier.height(12.dp));Text("按现有库存可制作",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);val possible=all.filter{canMake(it,bottles)};Text(if(possible.isEmpty())"暂时没有完全匹配的配方" else possible.joinToString(" · "){it.name});Spacer(Modifier.height(14.dp));Text("批量出杯",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);Row(verticalAlignment=Alignment.CenterVertically){Text("$batch 杯");Spacer(Modifier.width(8.dp));Button({batch=(batch-1).coerceAtLeast(1)}){Text("−")};Spacer(Modifier.width(5.dp));Button({batch++}){Text("＋")}};Text("示例：60 ml → ${60*batch} ml")}}

@Composable private fun BottleDialog(onDismiss:()->Unit,onSave:(Bottle)->Unit){var name by remember{mutableStateOf("")};var total by remember{mutableStateOf("750")};var remain by remember{mutableStateOf("750")};var price by remember{mutableStateOf("100")};var abv by remember{mutableStateOf("40")};AlertDialog(onDismissRequest=onDismiss,title={Text("添加酒瓶")},text={Column{OutlinedTextField(name,{name=it},label={Text("酒名/材料名")});OutlinedTextField(total,{total=it},label={Text("总容量 ml")});OutlinedTextField(remain,{remain=it},label={Text("剩余 ml")});OutlinedTextField(price,{price=it},label={Text("购买价格 ¥")});OutlinedTextField(abv,{abv=it},label={Text("ABV %")})}},confirmButton={Button({if(name.isNotBlank())onSave(Bottle(name,total.toDoubleOrNull()?:750,remain.toDoubleOrNull()?:750,price.toDoubleOrNull()?:0.0,abv.toDoubleOrNull()?:0.0))}){Text("保存")}},dismissButton={TextButton(onDismiss){Text("取消")}})}

private fun parseIngredients(s:String)=s.split(';').mapNotNull{p->val x=p.split(':',limit=2);if(x.size==2)Ingredient(x[0].trim(),x[1].trim())else null}
private fun parseCsv(t:String):List<Cocktail>{val l=t.lines().filter{it.isNotBlank()};return l.drop(if(l.firstOrNull()?.lowercase()?.contains("name")==true)1 else 0).mapNotNull{p->val x=p.split(',');if(x.size<6)null else Cocktail("import-${System.nanoTime()}",x[0].trim(),x[1].trim(),x[2].trim(),x[3].trim(),x[4].trim(),parseIngredients(x[5]),true)}}
private fun scaleAmount(s:String,n:Int):String{val m=Regex("([0-9]+(?:\\.[0-9]+)?)\\s*(ml|cl|oz)",RegexOption.IGNORE_CASE).find(s)?:return s;val v=m.groupValues[1].toDouble()*n;val out=if(v%1==0.0)v.toInt().toString() else String.format(Locale.US,"%.1f",v);return s.replaceRange(m.range,"$out ${m.groupValues[2]}")}
private fun numericMl(s:String):Double?=Regex("([0-9]+(?:\\.[0-9]+)?)\\s*ml",RegexOption.IGNORE_CASE).find(s)?.groupValues?.get(1)?.toDoubleOrNull()
private fun norm(s:String)=s.lowercase().replace("/"," ").replace("-"," ").trim()
private fun match(name:String,b:Bottle)=norm(name)==norm(b.name)||norm(name).contains(norm(b.name))||norm(b.name).contains(norm(name))
private fun bottleFor(name:String,bs:List<Bottle>)=bs.firstOrNull{match(name,it)}
private fun recipeCost(c:Cocktail,bs:List<Bottle>):Double{var cost=0.0;for(i in c.ingredients){val ml=numericMl(i.amount)?:continue;val b=bottleFor(i.name,bs)?:return -1.0;if(b.totalMl<=0)return -1.0;cost+=ml*b.price/b.totalMl};return cost}
private fun recipeAbv(c:Cocktail,bs:List<Bottle>):Double{var total=0.0;var alc=0.0;for(i in c.ingredients){val ml=numericMl(i.amount)?:continue;total+=ml;val b=bottleFor(i.name,bs);if(b!=null)alc+=ml*b.abv/100.0};return if(total>0)alc/total*100 else -1.0}
private fun canMake(c:Cocktail,bs:List<Bottle>):Boolean{for(i in c.ingredients){val ml=numericMl(i.amount)?:continue;val b=bottleFor(i.name,bs)?:return false;if(b.remainingMl+0.001<ml)return false};return true}
private fun fmt(v:Double)=String.format(Locale.US,"%.1f",v)
private fun toggleFav(s:Set<String>,id:String)=if(s.contains(id))s-id else s+id
private fun loadCocktails(c:Context):List<Cocktail>{val a=JSONArray(c.getSharedPreferences("lab",0).getString("cocktails","[]"));return buildList{for(i in 0 until a.length())add(jsonCocktail(a.getJSONObject(i)))}}
private fun saveCocktails(c:Context,l:List<Cocktail>){c.getSharedPreferences("lab",0).edit().putString("cocktails",JSONArray(l.map{cocktailJson(it)}).toString()).apply()}
private fun loadBottles(c:Context):List<Bottle>{val a=JSONArray(c.getSharedPreferences("lab",0).getString("bottles","[]"));return buildList{for(i in 0 until a.length())add(jsonBottle(a.getJSONObject(i)))}}
private fun saveBottles(c:Context,l:List<Bottle>){c.getSharedPreferences("lab",0).edit().putString("bottles",JSONArray(l.map{bottleJson(it)}).toString()).apply()}
private fun loadFavorites(c:Context)=c.getSharedPreferences("lab",0).getStringSet("favorites",emptySet())?:emptySet()
private fun saveFavorites(c:Context,s:Set<String>){c.getSharedPreferences("lab",0).edit().putStringSet("favorites",s).apply()}
