package com.chatmig.modern

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.File
import java.text.DateFormat
import java.util.Date

class MainActivity:ComponentActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);setContent{LoginScreen()}}}
@Composable private fun LoginScreen(){var email by remember{mutableStateOf("")};var pass by remember{mutableStateOf("")};var reg by remember{mutableStateOf(false)};var msg by remember{mutableStateOf("")};val a=remember{FirebaseAuth.getInstance()};val c=LocalContext.current
 LaunchedEffect(Unit){if(a.currentUser!=null){c.startActivity(Intent(c,HomeActivity::class.java));(c as? ComponentActivity)?.finish()}}
 Surface(Modifier.fillMaxSize()){Column(Modifier.fillMaxSize().padding(24.dp),Arrangement.Center){Text("Chat Mig33",style=MaterialTheme.typography.headlineLarge);Spacer(Modifier.height(18.dp));OutlinedTextField(email,{email=it},Modifier.fillMaxWidth(),label={Text("Enter the email")});Spacer(Modifier.height(10.dp));OutlinedTextField(pass,{pass=it},Modifier.fillMaxWidth(),label={Text("Enter the password")});Spacer(Modifier.height(14.dp));Button({if(reg)a.createUserWithEmailAndPassword(email.trim(),pass).addOnSuccessListener{c.startActivity(Intent(c,HomeActivity::class.java))}.addOnFailureListener{msg=it.localizedMessage?:"فشل التسجيل"} else a.signInWithEmailAndPassword(email.trim(),pass).addOnSuccessListener{c.startActivity(Intent(c,HomeActivity::class.java));(c as?ComponentActivity)?.finish()}.addOnFailureListener{msg=it.localizedMessage?:"فشل الدخول"}},Modifier.fillMaxWidth()){Text(if(reg)"register now" else "Login")};TextButton({reg=!reg}){Text(if(reg)"Login" else "Create now")};if(msg.isNotBlank())Text(msg)}}}

class HomeActivity:ComponentActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);setContent{HomeApp(intent.getStringExtra("chatPeer"))}}}
data class Dest(val route:String,val title:String,val icon:androidx.compose.ui.graphics.vector.ImageVector)
val dests=listOf(Dest("home","الرئيسية",Icons.Default.Home),Dest("users","المستخدمون",Icons.Default.People),Dest("rooms","الغرف",Icons.Default.MeetingRoom),Dest("profile","الملف الشخصي",Icons.Default.Person))
@Composable private fun HomeApp(openPeer:String?){val nav=rememberNavController();var selected by remember{mutableStateOf("home")};LaunchedEffect(openPeer){if(!openPeer.isNullOrBlank())nav.navigate("chat/$openPeer")};Scaffold(topBar={TopAppBar(title={Text("Chat Mig33")})},bottomBar={NavigationBar{dests.forEach{d->NavigationBarItem(selected==d.route,{selected=d.route;nav.navigate(d.route){popUpTo("home")}},icon={Icon(d.icon,null)},label={Text(d.title)})}}}){p->NavHost(nav,"home",Modifier.padding(p)){composable("home"){Home(nav)};composable("users"){Users(nav)};composable("rooms"){Rooms(nav)};composable("profile"){Profile(nav, FirebaseAuth.getInstance().uid.orEmpty())};composable("profile/{id}"){b->Profile(nav,b.arguments?.getString("id").orEmpty())};composable("chat/{id}"){b->Chat(b.arguments?.getString("id").orEmpty(),nav)};composable("room/{id}"){b->RoomChat(b.arguments?.getString("id").orEmpty(),nav)};composable("settings"){Settings()};composable("comments"){Comments()};composable("points"){Points(nav)};composable("merchant"){MerchantScreen(nav)};composable("transactions"){Transactions()}}}}
@Composable private fun Home(nav:NavHostController)=Page("Chat Mig33"){Text("looking for friends");Button({nav.navigate("users")}){Text("Users")};Button({nav.navigate("rooms")}){Text("showroom")};Button({nav.navigate("chat/private")}){Text("Private talk")};Button({nav.navigate("settings")}){Text("Open Setting")};Button({nav.navigate("points")}){Text("Number of points")};Button({nav.navigate("merchant")}){Text("Merchant")}}
@Composable private fun Users(nav:NavHostController){var q by remember{mutableStateOf("")};var users by remember{mutableStateOf(listOf<ChatUser>())};LaunchedEffect(Unit){Repo().users().limitToFirst(200).get().addOnSuccessListener{s->users=s.children.mapNotNull{it.getValue(ChatUser::class.java)}}};Page("Users"){OutlinedTextField(q,{q=it},Modifier.fillMaxWidth(),label={Text("Search")});LazyColumn{items(users.filter{it.name.contains(q,true)||it.uid.contains(q,true)}){u->Row(Modifier.fillMaxWidth().padding(vertical=6.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Person,null);Column(Modifier.weight(1f).padding(start=8.dp)){Text(u.name.ifBlank{u.uid});Text(if(u.online)"online" else "offline",style=MaterialTheme.typography.labelSmall)};TextButton({nav.navigate("profile/${u.uid}")}){Text("Profile")};TextButton({nav.navigate("chat/${u.uid}")}){Text("Chat")}}};if(users.isEmpty())item{Text("No users")}}}}
@Composable private fun Rooms(nav:NavHostController){
    val repo=remember{RoomRepo()}; var rooms by remember{mutableStateOf(listOf<ChatRoomModel>())}; var showCreate by remember{mutableStateOf(false)}; var error by remember{mutableStateOf("")}
    LaunchedEffect(Unit){repo.list().limitToFirst(100).get().addOnSuccessListener{s->rooms=s.children.mapNotNull{it.getValue(ChatRoomModel::class.java)}}}
    Page("Chat Rooms"){
        Button({showCreate=true},Modifier.fillMaxWidth()){Text("Create a chat room")}
        if(error.isNotBlank())Text(error)
        LazyColumn(Modifier.fillMaxWidth().weight(1f)){items(rooms){r->Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){Column(Modifier.padding(12.dp)){Text(r.name.ifBlank{"Unnamed room"},style=MaterialTheme.typography.titleMedium);if(r.topic.isNotBlank())Text(r.topic);Text("Members: ${r.memberCount}");Row{Button({repo.join(r.roomId){ok,e->if(ok)nav.navigate("room/${r.roomId}") else error=e.orEmpty()}}){Text("Enter")};Spacer(Modifier.width(8.dp));TextButton({nav.navigate("room/${r.roomId}")}){Text("Details")}}}}};if(rooms.isEmpty())item{Text("No rooms yet")}}
    }
    if(showCreate)CreateRoomDialog({showCreate=false}){name,topic->repo.create(name,topic){id,e->showCreate=false;if(id!=null)nav.navigate("room/$id") else error=e.orEmpty()}}
}

@Composable private fun CreateRoomDialog(close:()->Unit,create:(String,String)->Unit){var name by remember{mutableStateOf("")};var topic by remember{mutableStateOf("")};AlertDialog(onDismissRequest=close,title={Text("Create a chat room")},text={Column{OutlinedTextField(name,{name=it},label={Text("Channel Name")});OutlinedTextField(topic,{topic=it},label={Text("Topic")})}},confirmButton={Button({if(name.trim().isNotEmpty())create(name,topic)}){Text("Create")}},dismissButton={TextButton(close){Text("Cancel")}})}

@Composable private fun Profile(nav:NavHostController, uid:String){
 val repo=remember{RelationshipRepo()}; val me=FirebaseAuth.getInstance().uid.orEmpty(); var user by remember{mutableStateOf(ChatUser(uid=uid))}; var following by remember{mutableStateOf(false)}; var error by remember{mutableStateOf("")}; var comment by remember{mutableStateOf("")}; var comments by remember{mutableStateOf(listOf<Comment>())}
 LaunchedEffect(uid){repo.profile(uid).get().addOnSuccessListener{s->user=s.getValue(ChatUser::class.java)?:user};repo.comments(uid).limitToLast(50).get().addOnSuccessListener{s->comments=s.children.mapNotNull{it.getValue(Comment::class.java)}.sortedBy{it.timestamp}};if(uid!=me)repo.observeFollowing(uid){following=it}}
 Page("Profile settings"){
   Text(user.name.ifBlank{uid},style=MaterialTheme.typography.headlineSmall);Text(if(user.country.isBlank())"Country not set" else user.country);Text("Level: ${user.level}")
   if(uid!=me){Row{Button({repo.follow(uid,!following){ok,e->if(ok)following=!following else error=e.orEmpty()}}){Text(if(following)"Unfollow" else "Follow")};Spacer(Modifier.width(8.dp));Button({nav.navigate("chat/$uid")}){Text("Private talk")}}}
   if(error.isNotBlank())Text(error)
   Text("Comments",style=MaterialTheme.typography.titleLarge)
   LazyColumn(Modifier.fillMaxWidth().heightIn(max=260.dp)){items(comments){c->Column(Modifier.fillMaxWidth().padding(6.dp)){Text(c.authorName);Text(c.text)}}}
   if(me.isNotBlank()){OutlinedTextField(comment,{comment=it},Modifier.fillMaxWidth(),label={Text("Write a comment")});Button({if(comment.trim().isNotEmpty())repo.addComment(uid,comment){ok,e->if(ok){comment="";repo.comments(uid).get().addOnSuccessListener{s->comments=s.children.mapNotNull{it.getValue(Comment::class.java)}.sortedBy{it.timestamp}}}else error=e.orEmpty()}}){Text("Comment")}}
   Button({nav.navigate("comments")}){Text("All comments")};Button({nav.navigate("settings")}){Text("Settings")};Button({FirebaseAuth.getInstance().signOut();nav.navigate("home"){popUpTo("home"){inclusive=true}}}){Text("Logout")}
 }
}

@Composable private fun Chat(peer:String,nav:NavHostController){val ctx=LocalContext.current;val repo=remember{Repo()};var text by remember{mutableStateOf("")};var msgs by remember{mutableStateOf(listOf<ChatMessage>())};var error by remember{mutableStateOf("")};var blocked by remember{mutableStateOf(false)};var showEmoji by remember{mutableStateOf(false)};var recording by remember{mutableStateOf(false)};var recorder by remember{mutableStateOf<MediaRecorder?>(null)};var audioFile by remember{mutableStateOf<File?>(null)}
 val pick=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){uri:Uri?->uri?.let{repo.uploadMedia(peer,it,"image"){ok,e->if(!ok)error=e.orEmpty()}}}
 val mic=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){granted->if(granted){val f=File(ctx.cacheDir,"chat_${System.currentTimeMillis()}.m4a");val r=MediaRecorder(ctx);r.setAudioSource(MediaRecorder.AudioSource.MIC);r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);r.setOutputFile(f.absolutePath);r.prepare();r.start();recorder=r;audioFile=f;recording=true}}
 DisposableEffect(peer){val l=repo.observeMessages(peer,{msgs=it},{error=it});onDispose{repo.removeListener(peer,l)}}
 LaunchedEffect(peer){if(peer!="private")repo.isBlocked(peer){blocked=it}}
 Page("Private talk"){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text("${if(peer=="private")"Private" else peer}");TextButton({repo.setBlocked(peer,!blocked){blocked=!blocked}}){Text(if(blocked)"Unblock" else "Block")}};if(error.isNotBlank())Text(error);LazyColumn(Modifier.weight(1f).fillMaxWidth(),reverseLayout=false){itemsIndexed(msgs){_,m->Column(Modifier.fillMaxWidth().padding(vertical=3.dp)){Text(if(m.senderId==FirebaseAuth.getInstance().uid)"You" else m.senderName);when(m.type){"image"->AsyncImage(model=m.mediaUrl,contentDescription=null,modifier=Modifier.size(180.dp));"audio"->AudioBubble(m.mediaUrl);"emoji"->AsyncImage(model="file:///android_asset/emoji/${m.assetName}",contentDescription=null,modifier=Modifier.size(42.dp));else->Text(m.text)};Text("${DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(m.timestamp))} • ${m.status}",style=MaterialTheme.typography.labelSmall)}}}
 if(showEmoji){EmojiPanel{asset->repo.sendEmoji(peer,asset){ok,e->if(!ok)error=e.orEmpty()};showEmoji=false}};Row(horizontalArrangement=Arrangement.spacedBy(4.dp),verticalAlignment=Alignment.CenterVertically){IconButton({pick.launch("image/*")}){Icon(Icons.Default.Image,null)};IconButton({showEmoji=!showEmoji}){Icon(Icons.Default.EmojiEmotions,null)};IconButton({if(recording){try{recorder?.stop();recorder?.release()}catch(_:Exception){};recording=false;audioFile?.let{repo.uploadMedia(peer,Uri.fromFile(it),"audio"){ok,e->if(!ok)error=e.orEmpty()}}}else{if(ContextCompat.checkSelfPermission(ctx,Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED){val f=File(ctx.cacheDir,"chat_${System.currentTimeMillis()}.m4a");val r=MediaRecorder(ctx);r.setAudioSource(MediaRecorder.AudioSource.MIC);r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);r.setOutputFile(f.absolutePath);r.prepare();r.start();recorder=r;audioFile=f;recording=true}else mic.launch(Manifest.permission.RECORD_AUDIO)}}){Icon(if(recording)Icons.Default.Stop else Icons.Default.Mic,null)};OutlinedTextField(text,{text=it},Modifier.weight(1f),placeholder={Text("message")});IconButton({repo.sendText(peer,text){ok,e->if(ok)text="" else error=e.orEmpty()}}){Icon(Icons.Default.Send,null)}}}}
@Composable private fun AudioBubble(url:String){var playing by remember{mutableStateOf(false)};val ctx=LocalContext.current;Button({val p=android.media.MediaPlayer();p.setDataSource(url);p.setOnCompletionListener{playing=false;p.release()};p.prepareAsync();p.setOnPreparedListener{it.start();playing=true}}){Text(if(playing)"Playing…":"▶ Voice")}}
@Composable private fun EmojiPanel(onPick:(String)->Unit){val ctx=LocalContext.current;val names=remember{ctx.assets.list("emoji")?.take(80)?:emptyList()};LazyVerticalGrid(GridCells.Fixed(8),Modifier.fillMaxWidth().height(220.dp)){items(names.size){i->val n=names[i];AsyncImage(model="file:///android_asset/emoji/$n",contentDescription=null,modifier=Modifier.padding(3.dp).size(34.dp).then(Modifier)) ;TextButton({onPick(n)},Modifier.size(40.dp)){}}}}
@Composable private fun RoomChat(roomId:String,nav:NavHostController){
    val repo=remember{RoomRepo()};val me=FirebaseAuth.getInstance().uid.orEmpty();var room by remember{mutableStateOf(ChatRoomModel(roomId=roomId))};var members by remember{mutableStateOf(listOf<RoomMember>())};var messages by remember{mutableStateOf(listOf<ChatMessage>())};var text by remember{mutableStateOf("")};var error by remember{mutableStateOf("")};var isOwner by remember{mutableStateOf(false)};var showMembers by remember{mutableStateOf(false)}
    LaunchedEffect(roomId){repo.room(roomId).get().addOnSuccessListener{room=it.getValue(ChatRoomModel::class.java)?:room};repo.members(roomId).get().addOnSuccessListener{s->members=s.children.mapNotNull{it.getValue(RoomMember::class.java)};isOwner=members.any{it.uid==me&&it.role=="owner"}}}
    DisposableEffect(roomId){val l=object:ValueEventListener{override fun onDataChange(s:DataSnapshot){messages=s.children.mapNotNull{x->ChatMessage(x.key.orEmpty(),x.child("senderId").getValue(String::class.java).orEmpty(),x.child("message_name").getValue(String::class.java).orEmpty(),x.child("message").getValue(String::class.java).orEmpty(),"text","","",x.child("message_time").getValue(Long::class.java)?:0L,x.child("status").getValue(String::class.java)? :"sent","")}.sortedBy{it.timestamp}};override fun onCancelled(e:DatabaseError){error=e.message}};repo.messages(roomId).addValueEventListener(l);onDispose{repo.messages(roomId).removeEventListener(l)}}
    Page(room.name.ifBlank{"Chat Room"}){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Column{Text("${room.topic}");Text("Members: ${room.memberCount}")};TextButton({showMembers=true}){Text("Members")}}
        if(error.isNotBlank())Text(error)
        LazyColumn(Modifier.weight(1f).fillMaxWidth()){items(messages){m->Column(Modifier.fillMaxWidth().padding(4.dp)){Text(m.senderName);Text(m.text);Text(DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(m.timestamp)))}}}
        Row{OutlinedTextField(text,{text=it},Modifier.weight(1f),placeholder={Text("message")});IconButton({val uid=FirebaseAuth.getInstance().uid;if(uid==null)error="يجب تسجيل الدخول" else if(text.trim().isNotEmpty()){val k=repo.messages(roomId).push().key;if(k!=null)repo.messages(roomId).child(k).setValue(mapOf("message_id" to k,"message" to text.trim(),"message_name" to (FirebaseAuth.getInstance().currentUser?.displayName?:"User"),"message_type" to "text","message_time" to ServerValue.TIMESTAMP,"senderId" to uid,"status" to "sent")).addOnFailureListener{error=it.localizedMessage.orEmpty()};text=""}}){Icon(Icons.Default.Send,null)}}
        Row{Button({repo.leave(roomId){nav.popBackStack()}}){Text("Leave")};if(isOwner){Spacer(Modifier.width(8.dp));Button({repo.raise(roomId){ok->if(!ok)error="Failed"}}){Text("Raise the chat room")};Spacer(Modifier.width(8.dp));Button({showMembers=true}){Text("Room settings")}}}
    }
    if(showMembers)RoomMembersDialog(roomId,members,isOwner,repo){showMembers=false}
}

@Composable private fun RoomMembersDialog(roomId:String,members:List<RoomMember>,owner:Boolean,repo:RoomRepo,close:()->Unit){AlertDialog(onDismissRequest=close,title={Text("Room members")},text={LazyColumn{items(members){m->Row(Modifier.fillMaxWidth().padding(vertical=5.dp),horizontalArrangement=Arrangement.SpaceBetween){Column(Modifier.weight(1f)){Text(m.name.ifBlank{m.uid});Text(m.role,style=MaterialTheme.typography.labelSmall)};if(owner&&m.role!="owner"){TextButton({repo.kick(roomId,m.uid){}}){Text("Kick")};TextButton({repo.ban(roomId,m.uid,true){}}){Text("Ban")};TextButton({repo.setRole(roomId,m.uid,"moderator") {}}){Text("Admin")}}}}}},confirmButton={TextButton(close){Text("Close")}})}

@Composable private fun Points(nav:NavHostController){
    val repo=remember{PointsRepo()}; val me=FirebaseAuth.getInstance().uid.orEmpty()
    var points by remember{mutableStateOf(0L)}; var message by remember{mutableStateOf("")}
    var receiver by remember{mutableStateOf("")}; var amount by remember{mutableStateOf("")}; var history by remember{mutableStateOf(listOf<PointTransaction>())}
    DisposableEffect(me){ if(me.isBlank()) onDispose{} else {val l=repo.observeBalance({points=it},{message=it});onDispose{repo.removeBalanceListener(l)}} }
    LaunchedEffect(me){if(me.isNotBlank())repo.transactions().get().addOnSuccessListener{s->history=s.children.mapNotNull{it.getValue(PointTransaction::class.java)}.sortedByDescending{it.createdAt}}}
    Page("Number of points"){
        Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){Text("Your balance",style=MaterialTheme.typography.titleMedium);Text("$points",style=MaterialTheme.typography.displaySmall);Text("points")}}
        Text("Transfer points",style=MaterialTheme.typography.titleMedium)
        OutlinedTextField(receiver,{receiver=it},Modifier.fillMaxWidth(),label={Text("Recipient user ID")})
        OutlinedTextField(amount,{amount=it.filter(Char::isDigit)},Modifier.fillMaxWidth(),label={Text("Points to send")})
        Button({val n=amount.toLongOrNull();if(n==null||n<=0)message="أدخل عدد نقاط صحيح" else repo.sendPoints(receiver.trim(),n){ok,e->message=if(ok)"تم تحويل $n نقطة بنجاح" else e.orEmpty();if(ok){receiver="";amount=""}}},Modifier.fillMaxWidth()){Text("Send points")}
        Text("You cannot send points to yourself")
        Text("You must have 3000 points before sending")
        Row(Modifier.fillMaxWidth()){Button({nav.navigate("merchant")},Modifier.weight(1f)){Text("Merchant")};Spacer(Modifier.width(8.dp));Button({nav.navigate("transactions")},Modifier.weight(1f)){Text("Transactions")}}
        if(message.isNotBlank())Text(message)
    }
}

@Composable private fun MerchantScreen(nav:NavHostController){
    val repo=remember{PointsRepo()};val me=FirebaseAuth.getInstance().uid.orEmpty();var items by remember{mutableStateOf(listOf<Merchant>())};var showAdd by remember{mutableStateOf(false)};var selected by remember{mutableStateOf<Merchant?>(null)};var error by remember{mutableStateOf("")}
    DisposableEffect(Unit){val l=object:ValueEventListener{override fun onDataChange(s:DataSnapshot){items=s.children.mapNotNull{it.getValue(Merchant::class.java)}};override fun onCancelled(e:DatabaseError){error=e.message}};repo.merchantList().addValueEventListener(l);onDispose{repo.merchantRef().removeEventListener(l)}}
    Page("Merchant"){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text("Merchant / Marchant",style=MaterialTheme.typography.titleLarge);Button({showAdd=true}){Text("Add")}}
        if(error.isNotBlank())Text(error)
        if(items.isEmpty())Text("No merchants yet") else LazyColumn(Modifier.weight(1f)){items(items){m->Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){Column(Modifier.padding(12.dp)){Text(m.name,style=MaterialTheme.typography.titleMedium);if(m.description.isNotBlank())Text(m.description);Text("Price: ${m.price} points");Text(if(m.active)"Active" else "Inactive");Row{if(m.ownerId!=me&&m.active)Button({selected=m}){Text("Buy")};if(m.ownerId==me){Spacer(Modifier.width(8.dp));TextButton({repo.setMerchantActive(m.merchantId,!m.active){ok,e->if(!ok)error=e.orEmpty()}}){Text(if(m.active)"Disable" else "Enable")};TextButton({repo.deleteMerchant(m.merchantId){ok,e->if(!ok)error=e.orEmpty()}}){Text("Delete")}}}}}}}
        TextButton({nav.navigate("transactions")}){Text("Transaction history")};TextButton({nav.navigateUp()}){Text("Back")}
    }
    if(showAdd)AddMerchantDialog({showAdd=false}){n,d,p->repo.addMerchant(n,d,p){ok,e->showAdd=false;if(!ok)error=e.orEmpty()}}
    selected?.let{m->AlertDialog(onDismissRequest={selected=null},title={Text("Confirm purchase")},text={Text("Buy ${m.name} for ${m.price} points?")},confirmButton={Button({repo.purchaseMerchant(m){ok,e->error=if(ok)"تم الشراء بنجاح" else e.orEmpty();if(ok)selected=null}}){Text("Buy")}},dismissButton={TextButton({selected=null}){Text("Cancel")}})}
}

@Composable private fun Transactions(){
    val repo=remember{PointsRepo()};val me=FirebaseAuth.getInstance().uid.orEmpty();var list by remember{mutableStateOf(listOf<PointTransaction>())};var error by remember{mutableStateOf("")}
    LaunchedEffect(me){if(me.isNotBlank())repo.transactions().get().addOnSuccessListener{s->list=s.children.mapNotNull{it.getValue(PointTransaction::class.java)}.sortedByDescending{it.createdAt}}.addOnFailureListener{error=it.localizedMessage.orEmpty()}}
    Page("Point transactions"){
        if(error.isNotBlank())Text(error)
        if(list.isEmpty())Text("No transactions yet") else LazyColumn(Modifier.weight(1f)){items(list){t->Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){Column(Modifier.padding(12.dp)){Text(if(t.type=="merchant_purchase")"Merchant purchase" else "Points transfer",style=MaterialTheme.typography.titleMedium);Text("Amount: ${t.amount} points");if(t.merchantName.isNotBlank())Text("Merchant: ${t.merchantName}");Text("From: ${t.fromId}");if(t.toId.isNotBlank())Text("To: ${t.toId}");Text("Status: ${t.status}")}}}}
    }
}

@Composable private fun AddMerchantDialog(close:()->Unit,create:(String,String,Long)->Unit){var name by remember{mutableStateOf("")};var desc by remember{mutableStateOf("")};var price by remember{mutableStateOf("")};AlertDialog(onDismissRequest=close,title={Text("Add merchant")},text={Column{OutlinedTextField(name,{name=it},label={Text("Merchant name")});OutlinedTextField(desc,{desc=it},label={Text("Description")});OutlinedTextField(price,{price=it.filter(Char::isDigit)},label={Text("Price in points")})}},confirmButton={Button({val p=price.toLongOrNull();if(name.trim().isNotEmpty()&&p!=null&&p>0)create(name,desc,p)}){Text("Save")}},dismissButton={TextButton(close){Text("Cancel")}})}
@Composable private fun Comments(){
 val repo=remember{RelationshipRepo()};val uid=FirebaseAuth.getInstance().uid.orEmpty();var text by remember{mutableStateOf("")};var list by remember{mutableStateOf(listOf<Comment>())};var msg by remember{mutableStateOf("")}
 LaunchedEffect(uid){if(uid.isNotBlank())repo.comments(uid).limitToLast(100).get().addOnSuccessListener{s->list=s.children.mapNotNull{it.getValue(Comment::class.java)}.sortedByDescending{it.timestamp}}}
 Page("Comments"){LazyColumn(Modifier.weight(1f)){items(list){c->Card(Modifier.fillMaxWidth().padding(3.dp)){Column(Modifier.padding(10.dp)){Text(c.authorName);Text(c.text)}}}};OutlinedTextField(text,{text=it},Modifier.fillMaxWidth(),label={Text("Comment")});Button({if(text.trim().isNotEmpty())repo.addComment(uid,text){ok,e->if(ok){text="";msg="تم إضافة التعليق"}else msg=e.orEmpty()}}){Text("Send")};if(msg.isNotBlank())Text(msg)}
}
@Composable private fun Settings(){
 val repo=remember{RelationshipRepo()}; val nrepo=remember{NotificationRepo()}; var blocked by remember{mutableStateOf(listOf<String>())}; var notifications by remember{mutableStateOf(listOf<AppNotification>())}; var message by remember{mutableStateOf("")};
 LaunchedEffect(Unit){repo.blockList().get().addOnSuccessListener{s->blocked=s.children.filter{it.getValue(Boolean::class.java)==true}.map{it.key.orEmpty()}};nrepo.inbox().limitToLast(50).get().addOnSuccessListener{s->notifications=s.children.mapNotNull{it.getValue(AppNotification::class.java)}.sortedByDescending{it.timestamp}}}
 Page("General settings"){
  Text("Notifications",style=MaterialTheme.typography.titleLarge)
  if(notifications.isEmpty()) Text("No notifications yet") else LazyColumn(Modifier.heightIn(max=220.dp)){items(notifications){n->Card(Modifier.fillMaxWidth().padding(vertical=3.dp)){Column(Modifier.padding(10.dp)){Text(n.title);Text(n.body);if(!n.read)TextButton({nrepo.markRead(n.id);notifications=notifications.map{if(it.id==n.id)it.copy(read=true)else it}}){Text("Mark as read")}}}}}
  Spacer(Modifier.height(8.dp)); Text("Block list",style=MaterialTheme.typography.titleMedium)
  if(blocked.isEmpty())Text("No blocked users") else LazyColumn(Modifier.heightIn(max=220.dp)){items(blocked){id->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(id);TextButton({repo.blockList().child(id).setValue(false);blocked=blocked.filterNot{it==id};message="Unblocked"}){Text("Unblock")}}}}
  Text("Profile settings");Text("English language");if(message.isNotBlank())Text(message)
 }
}

@Composable private fun Page(title:String,content:@Composable ColumnScope.()->Unit){Column(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text(title,style=MaterialTheme.typography.headlineMedium);content()}}
