@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

// ═══════════ MainActivity ═══════════
class MainActivity : ComponentActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(
                primary = Color(0xFF6750A4),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFEADDFF),
                secondary = Color(0xFF625B71)
            )) { LoginScreen() }
        }
    }
}

// ═══════════ تسجيل الدخول ═══════════
@Composable
private fun LoginScreen() {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var reg by remember { mutableStateOf(false) }
    var msg by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var showPass by remember { mutableStateOf(false) }
    val a = remember { FirebaseAuth.getInstance() }
    val c = LocalContext.current
    val repo = remember { Repo() }

    LaunchedEffect(Unit) {
        if (a.currentUser != null) {
            repo.ensureProfile {}
            c.startActivity(Intent(c, HomeActivity::class.java))
            (c as? ComponentActivity)?.finish()
        }
    }

    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier.size(100.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ChatBubble, null, Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(20.dp))
            Text("شات ميج 33", fontSize = 32.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Text("تواصل، دردش، وشارك", fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(30.dp))

            OutlinedTextField(
                email, { email = it }, Modifier.fillMaxWidth(),
                label = { Text("البريد الإلكتروني") },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                pass, { pass = it }, Modifier.fillMaxWidth(),
                label = { Text("كلمة المرور") },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    IconButton({ showPass = !showPass }) {
                        Icon(if (showPass) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility, null)
                    }
                },
                singleLine = true,
                visualTransformation = if (showPass) VisualTransformation.None
                    else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (email.isBlank() || pass.isBlank()) {
                        msg = "أدخل البريد وكلمة المرور"; return@Button
                    }
                    loading = true; msg = ""
                    if (reg) {
                        a.createUserWithEmailAndPassword(email.trim(), pass)
                            .addOnSuccessListener {
                                repo.ensureProfile {}
                                loading = false
                                c.startActivity(Intent(c, HomeActivity::class.java))
                                (c as? ComponentActivity)?.finish()
                            }
                            .addOnFailureListener {
                                loading = false
                                msg = "فشل التسجيل: ${it.localizedMessage ?: "خطأ"}"
                            }
                    } else {
                        a.signInWithEmailAndPassword(email.trim(), pass)
                            .addOnSuccessListener {
                                repo.ensureProfile {}
                                loading = false
                                c.startActivity(Intent(c, HomeActivity::class.java))
                                (c as? ComponentActivity)?.finish()
                            }
                            .addOnFailureListener {
                                loading = false
                                msg = "فشل الدخول: ${it.localizedMessage ?: "خطأ"}"
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !loading
            ) {
                if (loading) {
                    CircularProgressIndicator(Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (reg) "إنشاء الحساب" else "تسجيل الدخول",
                    fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            TextButton({ reg = !reg; msg = "" }) {
                Text(if (reg) "لديك حساب؟ سجل الدخول"
                    else "ليس لديك حساب؟ أنشئ واحدًا")
            }
            if (msg.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(msg, Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

// ═══════════ HomeActivity ═══════════
class HomeActivity : ComponentActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContent { HomeApp(intent.getStringExtra("chatPeer")) }
    }
}

data class Dest(val route: String, val title: String,
                val icon: androidx.compose.ui.graphics.vector.ImageVector)

val dests = listOf(
    Dest("home", "الرئيسية", Icons.Default.Home),
    Dest("users", "المستخدمون", Icons.Default.People),
    Dest("rooms", "الغرف", Icons.Default.MeetingRoom),
    Dest("profile", "حسابي", Icons.Default.Person)
)

@Composable
private fun HomeApp(openPeer: String?) {
    val nav = rememberNavController()
    var selected by remember { mutableStateOf("home") }
    val repo = remember { Repo() }
    val myUid = FirebaseAuth.getInstance().uid.orEmpty()

    LaunchedEffect(myUid) {
        if (myUid.isNotBlank()) repo.setOnline(myUid, true)
    }
    DisposableEffect(Unit) {
        onDispose { if (myUid.isNotBlank()) repo.setOnline(myUid, false) }
    }

    LaunchedEffect(openPeer) {
        if (!openPeer.isNullOrBlank()) nav.navigate("chat/$openPeer")
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ChatBubble, null,
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("شات ميج 33", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        },
        bottomBar = {
            NavigationBar {
                dests.forEach { d ->
                    NavigationBarItem(
                        selected = selected == d.route,
                        onClick = { selected = d.route; nav.navigate(d.route) { popUpTo("home") } },
                        icon = { Icon(d.icon, null) },
                        label = { Text(d.title, fontSize = 11.sp) }
                    )
                }
            }
        }
    ) { p ->
        NavHost(nav, "home", Modifier.padding(p)) {
            composable("home") { Home(nav) }
            composable("users") { Users(nav) }
            composable("rooms") { Rooms(nav) }
            composable("profile") { Profile(nav, myUid) }
            composable("profile/{id}") { b ->
                Profile(nav, b.arguments?.getString("id").orEmpty())
            }
            composable("editProfile") { EditProfileScreen(nav) }
            composable("chat/{id}") { b ->
                Chat(b.arguments?.getString("id").orEmpty(), nav)
            }
            composable("room/{id}") { b ->
                RoomChat(b.arguments?.getString("id").orEmpty(), nav)
            }
            composable("settings") { Settings() }
            composable("comments") { Comments() }
            composable("points") { Points(nav) }
            composable("merchant") { MerchantScreen(nav) }
            composable("transactions") { Transactions() }
        }
    }
}

// ═══════════ الرئيسية ═══════════
data class HomeOpt(val title: String, val subtitle: String,
                   val icon: androidx.compose.ui.graphics.vector.ImageVector,
                   val color: Color, val route: String)

val homeOptions = listOf(
    HomeOpt("المستخدمون", "اكتشف أصدقاء", Icons.Default.People, Color(0xFF6750A4), "users"),
    HomeOpt("الغرف", "دردشة جماعية", Icons.Default.MeetingRoom, Color(0xFF00897B), "rooms"),
    HomeOpt("محادثة خاصة", "دردش سرًا", Icons.Default.ChatBubble, Color(0xFFE91E63), "chat/private"),
    HomeOpt("الإعدادات", "تحكم بحسابك", Icons.Default.Settings, Color(0xFF546E7A), "settings"),
    HomeOpt("رصيد النقاط", "أموالي", Icons.Default.Star, Color(0xFFFFA000), "points"),
    HomeOpt("المتجر", "اشترِ وبع", Icons.Default.ShoppingCart, Color(0xFF7B1FA2), "merchant")
)

@Composable
private fun Home(nav: NavHostController) {
    val myUid = FirebaseAuth.getInstance().uid.orEmpty()
    val repo = remember { Repo() }
    var me by remember { mutableStateOf(ChatUser(uid = myUid)) }
    var greet by remember { mutableStateOf("صديقي") }

    LaunchedEffect(myUid) {
        if (myUid.isNotBlank()) {
            repo.user(myUid).get().addOnSuccessListener { s ->
                s.getValue(ChatUser::class.java)?.let {
                    me = it
                    if (it.name.isNotBlank()) greet = it.name
                }
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(me, 56.dp, online = true)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("أهلاً بك، $greet 👋", fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.height(4.dp))
                    Text("ابدأ رحلتك في عالم الدردشة", fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                }
                IconButton({ nav.navigate("editProfile") }) {
                    Icon(Icons.Default.Edit, null,
                        tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        Text("الخيارات السريعة", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            gridItems(homeOptions) { o ->
                HomeCard(o.title, o.subtitle, o.icon, o.color) { nav.navigate(o.route) }
            }
        }
    }
}

@Composable
private fun HomeCard(title: String, subtitle: String,
                     icon: androidx.compose.ui.graphics.vector.ImageVector,
                     color: Color, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(130.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(color),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Column {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

// ═══════════ Avatar ═══════════
@Composable
private fun UserAvatar(user: ChatUser, size: androidx.compose.ui.unit.Dp,
                       online: Boolean = false) {
    val colors = listOf(Color(0xFF6750A4), Color(0xFF00897B), Color(0xFFE91E63),
        Color(0xFFFFA000), Color(0xFF546E7A), Color(0xFF7B1FA2))
    val fallbackName = user.name.ifBlank { user.uid }.ifBlank { "؟" }
    val bg = colors[(fallbackName.hashCode().absoluteValue) % colors.size]

    Box {
        if (user.photoUrl.isNotBlank()) {
            AsyncImage(
                model = user.photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size).clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape)
            )
        } else {
            Box(Modifier.size(size).clip(CircleShape).background(bg),
                contentAlignment = Alignment.Center) {
                Text(fallbackName.take(1).uppercase(), color = Color.White,
                    fontWeight = FontWeight.Bold, fontSize = (size.value / 2.5f).sp)
            }
        }
        if (online) {
            Box(Modifier.size(size / 4).clip(CircleShape)
                .background(Color(0xFF4CAF50))
                .border(2.dp, Color.White, CircleShape)
                .align(Alignment.BottomEnd))
        }
    }
}

// ═══════════ المستخدمون ═══════════
@Composable
private fun Users(nav: NavHostController) {
    var q by remember { mutableStateOf("") }
    var users by remember { mutableStateOf(listOf<ChatUser>()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        Repo().users().limitToFirst(200).get().addOnSuccessListener { s ->
            users = s.children.mapNotNull { it.getValue(ChatUser::class.java) }
            loading = false
        }.addOnFailureListener { loading = false }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("المستخدمون", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(q, { q = it }, Modifier.fillMaxWidth(),
            placeholder = { Text("ابحث عن مستخدم...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (q.isNotEmpty()) IconButton({ q = "" }) {
                    Icon(Icons.Default.Clear, null)
                }
            },
            singleLine = true, shape = RoundedCornerShape(28.dp))
        Spacer(Modifier.height(12.dp))

        val filtered = users.filter {
            it.name.contains(q, true) || it.uid.contains(q, true)
        }
        when {
            loading -> LoadingBox()
            filtered.isEmpty() -> EmptyState(Icons.Default.PersonOff, "لا يوجد مستخدمون",
                if (q.isBlank()) "لم ينضم أحد بعد" else "لا نتائج للبحث")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered) { u -> UserRow(u, nav) }
            }
        }
    }
}

@Composable
private fun UserRow(u: ChatUser, nav: NavHostController) {
    Card(modifier = Modifier.fillMaxWidth(),
        onClick = { nav.navigate("profile/${u.uid}") }) {
        Row(Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            UserAvatar(u, 48.dp, u.online)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(u.name.ifBlank { "مستخدم" },
                    fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(if (u.online) "متصل الآن" else "غير متصل", fontSize = 12.sp,
                    color = if (u.online) Color(0xFF4CAF50) else Color.Gray)
            }
            IconButton({ nav.navigate("chat/${u.uid}") }) {
                Icon(Icons.Default.ChatBubble, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// ═══════════ الغرف ═══════════
@Composable
private fun Rooms(nav: NavHostController) {
    val repo = remember { RoomRepo() }
    var rooms by remember { mutableStateOf(listOf<ChatRoomModel>()) }
    var showCreate by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        repo.list().limitToFirst(100).get().addOnSuccessListener { s ->
            rooms = s.children.mapNotNull { it.getValue(ChatRoomModel::class.java) }
            loading = false
        }.addOnFailureListener { loading = false }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("الغرف", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Button({ showCreate = true }, shape = RoundedCornerShape(24.dp)) {
                Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("إنشاء")
            }
        }
        if (error.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text(error, Modifier.padding(10.dp), fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        when {
            loading -> LoadingBox()
            rooms.isEmpty() -> EmptyState(Icons.Default.MeetingRoom, "لا توجد غرف",
                "أنشئ أول غرفة دردشة الآن!")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(rooms) { r -> RoomCard(r, nav, repo) { error = it } }
            }
        }
    }

    if (showCreate) {
        CreateRoomDialog({ showCreate = false }) { n, t ->
            repo.create(n, t) { id, e ->
                showCreate = false
                if (id != null) nav.navigate("room/$id") else error = e.orEmpty()
            }
        }
    }
}

@Composable
private fun RoomCard(r: ChatRoomModel, nav: NavHostController,
                     repo: RoomRepo, onError: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).clip(CircleShape)
                    .background(Color(0xFF00897B).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.MeetingRoom, null, tint = Color(0xFF00897B))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(r.name.ifBlank { "غرفة بدون اسم" },
                        fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (r.topic.isNotBlank())
                        Text(r.topic, fontSize = 12.sp, color = Color.Gray)
                }
                AssistChip(onClick = {}, label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.People, null, Modifier.size(14.dp),
                            tint = Color(0xFF00897B))
                        Spacer(Modifier.width(4.dp))
                        Text("${r.memberCount}", fontSize = 12.sp)
                    }
                })
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button({
                    repo.join(r.roomId) { ok, e ->
                        if (ok) nav.navigate("room/${r.roomId}") else onError(e.orEmpty())
                    }
                }, Modifier.weight(1f), shape = RoundedCornerShape(20.dp)) {
                    Icon(Icons.Default.Login, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("دخول")
                }
                OutlinedButton({ nav.navigate("room/${r.roomId}") },
                    shape = RoundedCornerShape(20.dp)) {
                    Icon(Icons.Default.Info, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("التفاصيل")
                }
            }
        }
    }
}

@Composable
private fun CreateRoomDialog(close: () -> Unit, create: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = close,
        icon = { Icon(Icons.Default.AddCircle, null,
            tint = MaterialTheme.colorScheme.primary) },
        title = { Text("إنشاء غرفة دردشة", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(),
                    label = { Text("اسم الغرفة") },
                    leadingIcon = { Icon(Icons.Default.Tag, null) }, singleLine = true)
                OutlinedTextField(topic, { topic = it }, Modifier.fillMaxWidth(),
                    label = { Text("الموضوع") },
                    leadingIcon = { Icon(Icons.Default.Subject, null) }, singleLine = true)
            }
        },
        confirmButton = {
            Button({ if (name.trim().isNotEmpty()) create(name, topic) },
                enabled = name.trim().isNotEmpty()) { Text("إنشاء") }
        },
        dismissButton = { TextButton(close) { Text("إلغاء") } }
    )
}

// ═══════════ تعديل الملف الشخصي ═══════════
@Composable
private fun EditProfileScreen(nav: NavHostController) {
    val repo = remember { Repo() }
    val myUid = FirebaseAuth.getInstance().uid.orEmpty()

    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var themeColor by remember { mutableStateOf("#6750A4") }
    var photoUrl by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var msg by remember { mutableStateOf("") }
    var uploadingPhoto by remember { mutableStateOf(false) }

    val colorOptions = listOf("#6750A4", "#00897B", "#E91E63", "#FFA000",
        "#546E7A", "#7B1FA2", "#1976D2", "#D32F2F")

    LaunchedEffect(myUid) {
        if (myUid.isBlank()) return@LaunchedEffect
        repo.user(myUid).get().addOnSuccessListener { s ->
            s.getValue(ChatUser::class.java)?.let {
                name = it.name
                bio = it.bio
                country = it.country
                themeColor = it.themeColor.ifBlank { "#6750A4" }
                photoUrl = it.photoUrl
            }
            loading = false
        }.addOnFailureListener { loading = false }
    }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            uploadingPhoto = true
            repo.uploadAvatar(myUid, it) { ok, url ->
                uploadingPhoto = false
                if (ok && url != null) {
                    photoUrl = url
                    msg = "✅ تم تحديث الصورة"
                } else {
                    msg = "فشل رفع الصورة (قد يحتاج Firebase Storage)"
                }
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        Surface(Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer) {
            Row(Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                IconButton({ nav.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Text("تعديل الملف الشخصي", fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        if (loading) {
            LoadingBox()
            return@Column
        }

        LazyColumn(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        if (uploadingPhoto) {
                            Box(Modifier.size(120.dp).clip(CircleShape)
                                .background(Color.LightGray),
                                contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else {
                            UserAvatar(
                                ChatUser(uid = myUid, name = name, photoUrl = photoUrl),
                                120.dp
                            )
                        }
                        Box(
                            Modifier.size(36.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable { photoPicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, null,
                                tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("اضغط على الكاميرا لتغيير الصورة",
                        fontSize = 12.sp, color = Color.Gray)
                }
            }

            item {
                OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(),
                    label = { Text("الاسم الكامل") },
                    leadingIcon = { Icon(Icons.Default.Person, null) }, singleLine = true)
            }
            item {
                OutlinedTextField(bio, { bio = it }, Modifier.fillMaxWidth(),
                    label = { Text("نبذة عنك") },
                    placeholder = { Text("اكتب شيئًا عن نفسك...") },
                    leadingIcon = { Icon(Icons.Default.Description, null) },
                    minLines = 3, maxLines = 5)
            }
            item {
                OutlinedTextField(country, { country = it }, Modifier.fillMaxWidth(),
                    label = { Text("الدولة") },
                    leadingIcon = { Icon(Icons.Default.Public, null) }, singleLine = true)
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Palette, null,
                                tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("لون الملف الشخصي",
                                fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly) {
                            colorOptions.take(4).forEach { hex ->
                                ColorCircle(hex, themeColor) { themeColor = hex }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly) {
                            colorOptions.drop(4).forEach { hex ->
                                ColorCircle(hex, themeColor) { themeColor = hex }
                            }
                        }
                    }
                }
            }
            if (msg.isNotBlank()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Text(msg, Modifier.padding(12.dp), fontSize = 13.sp)
                    }
                }
            }
            item {
                Button(
                    onClick = {
                        if (name.trim().isBlank()) {
                            msg = "الاسم مطلوب"; return@Button
                        }
                        saving = true
                        repo.updateProfile(name.trim(), bio.trim(),
                            country.trim(), themeColor) { ok, e ->
                            saving = false
                            msg = if (ok) "✅ تم حفظ التغييرات"
                                else "فشل: ${e ?: "خطأ"}"
                        }
                    },
                    Modifier.fillMaxWidth().height(52.dp),
                    enabled = !saving
                ) {
                    if (saving) {
                        CircularProgressIndicator(Modifier.size(20.dp),
                            color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    } else {
                        Icon(Icons.Default.Save, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("حفظ التغييرات", fontSize = 16.sp,
                        fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ColorCircle(hex: String, selected: String, onClick: () -> Unit) {
    val color = try { Color(android.graphics.Color.parseColor(hex)) }
        catch (_: Exception) { Color.Gray }
    val isSelected = hex == selected
    Box(
        Modifier.size(52.dp).clip(CircleShape).background(color)
            .border(
                width = if (isSelected) 4.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface
                    else Color.Transparent,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(Icons.Default.Check, null, tint = Color.White,
                modifier = Modifier.size(24.dp))
        }
    }
}

// ═══════════ الملف الشخصي ═══════════
@Composable
private fun Profile(nav: NavHostController, uid: String) {
    val repo = remember { RelationshipRepo() }
    val mainRepo = remember { Repo() }
    val me = FirebaseAuth.getInstance().uid.orEmpty()
    var user by remember { mutableStateOf(ChatUser(uid = uid)) }
    var following by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var comments by remember { mutableStateOf(listOf<Comment>()) }

    LaunchedEffect(uid) {
        mainRepo.observeProfile(uid) { user = it }
        repo.comments(uid).limitToLast(50).get().addOnSuccessListener { s ->
            comments = s.children.mapNotNull { it.getValue(Comment::class.java) }
                .sortedByDescending { it.timestamp }
        }
        if (uid != me) repo.observeFollowing(uid) { following = it }
    }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Column(Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally) {
                UserAvatar(user, 100.dp, user.online)
                Spacer(Modifier.height(12.dp))
                Text(user.name.ifBlank { "مستخدم" },
                    fontSize = 24.sp, fontWeight = FontWeight.Bold)
                if (user
                    @file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

// ═══════════ MainActivity ═══════════
class MainActivity : ComponentActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(
                primary = Color(0xFF6750A4),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFEADDFF),
                secondary = Color(0xFF625B71)
            )) { LoginScreen() }
        }
    }
}

// ═══════════ تسجيل الدخول ═══════════
@Composable
private fun LoginScreen() {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var reg by remember { mutableStateOf(false) }
    var msg by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var showPass by remember { mutableStateOf(false) }
    val a = remember { FirebaseAuth.getInstance() }
    val c = LocalContext.current
    val repo = remember { Repo() }

    LaunchedEffect(Unit) {
        if (a.currentUser != null) {
            repo.ensureProfile {}
            c.startActivity(Intent(c, HomeActivity::class.java))
            (c as? ComponentActivity)?.finish()
        }
    }

    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier.size(100.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ChatBubble, null, Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(20.dp))
            Text("شات ميج 33", fontSize = 32.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Text("تواصل، دردش، وشارك", fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(30.dp))

            OutlinedTextField(
                email, { email = it }, Modifier.fillMaxWidth(),
                label = { Text("البريد الإلكتروني") },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                pass, { pass = it }, Modifier.fillMaxWidth(),
                label = { Text("كلمة المرور") },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    IconButton({ showPass = !showPass }) {
                        Icon(if (showPass) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility, null)
                    }
                },
                singleLine = true,
                visualTransformation = if (showPass) VisualTransformation.None
                    else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (email.isBlank() || pass.isBlank()) {
                        msg = "أدخل البريد وكلمة المرور"; return@Button
                    }
                    loading = true; msg = ""
                    if (reg) {
                        a.createUserWithEmailAndPassword(email.trim(), pass)
                            .addOnSuccessListener {
                                repo.ensureProfile {}
                                loading = false
                                c.startActivity(Intent(c, HomeActivity::class.java))
                                (c as? ComponentActivity)?.finish()
                            }
                            .addOnFailureListener {
                                loading = false
                                msg = "فشل التسجيل: ${it.localizedMessage ?: "خطأ"}"
                            }
                    } else {
                        a.signInWithEmailAndPassword(email.trim(), pass)
                            .addOnSuccessListener {
                                repo.ensureProfile {}
                                loading = false
                                c.startActivity(Intent(c, HomeActivity::class.java))
                                (c as? ComponentActivity)?.finish()
                            }
                            .addOnFailureListener {
                                loading = false
                                msg = "فشل الدخول: ${it.localizedMessage ?: "خطأ"}"
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !loading
            ) {
                if (loading) {
                    CircularProgressIndicator(Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (reg) "إنشاء الحساب" else "تسجيل الدخول",
                    fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            TextButton({ reg = !reg; msg = "" }) {
                Text(if (reg) "لديك حساب؟ سجل الدخول"
                    else "ليس لديك حساب؟ أنشئ واحدًا")
            }
            if (msg.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(msg, Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

// ═══════════ HomeActivity ═══════════
class HomeActivity : ComponentActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContent { HomeApp(intent.getStringExtra("chatPeer")) }
    }
}

data class Dest(val route: String, val title: String,
                val icon: androidx.compose.ui.graphics.vector.ImageVector)

val dests = listOf(
    Dest("home", "الرئيسية", Icons.Default.Home),
    Dest("users", "المستخدمون", Icons.Default.People),
    Dest("rooms", "الغرف", Icons.Default.MeetingRoom),
    Dest("profile", "حسابي", Icons.Default.Person)
)

@Composable
private fun HomeApp(openPeer: String?) {
    val nav = rememberNavController()
    var selected by remember { mutableStateOf("home") }
    val repo = remember { Repo() }
    val myUid = FirebaseAuth.getInstance().uid.orEmpty()

    LaunchedEffect(myUid) {
        if (myUid.isNotBlank()) repo.setOnline(myUid, true)
    }
    DisposableEffect(Unit) {
        onDispose { if (myUid.isNotBlank()) repo.setOnline(myUid, false) }
    }

    LaunchedEffect(openPeer) {
        if (!openPeer.isNullOrBlank()) nav.navigate("chat/$openPeer")
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ChatBubble, null,
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("شات ميج 33", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        },
        bottomBar = {
            NavigationBar {
                dests.forEach { d ->
                    NavigationBarItem(
                        selected = selected == d.route,
                        onClick = { selected = d.route; nav.navigate(d.route) { popUpTo("home") } },
                        icon = { Icon(d.icon, null) },
                        label = { Text(d.title, fontSize = 11.sp) }
                    )
                }
            }
        }
    ) { p ->
        NavHost(nav, "home", Modifier.padding(p)) {
            composable("home") { Home(nav) }
            composable("users") { Users(nav) }
            composable("rooms") { Rooms(nav) }
            composable("profile") { Profile(nav, myUid) }
            composable("profile/{id}") { b ->
                Profile(nav, b.arguments?.getString("id").orEmpty())
            }
            composable("editProfile") { EditProfileScreen(nav) }
            composable("chat/{id}") { b ->
                Chat(b.arguments?.getString("id").orEmpty(), nav)
            }
            composable("room/{id}") { b ->
                RoomChat(b.arguments?.getString("id").orEmpty(), nav)
            }
            composable("settings") { Settings() }
            composable("comments") { Comments() }
            composable("points") { Points(nav) }
            composable("merchant") { MerchantScreen(nav) }
            composable("transactions") { Transactions() }
        }
    }
}

// ═══════════ الصفحة الرئيسية ═══════════
data class HomeOpt(val title: String, val subtitle: String,
                   val icon: androidx.compose.ui.graphics.vector.ImageVector,
                   val color: Color, val route: String)

val homeOptions = listOf(
    HomeOpt("المستخدمون", "اكتشف أصدقاء", Icons.Default.People, Color(0xFF6750A4), "users"),
    HomeOpt("الغرف", "دردشة جماعية", Icons.Default.MeetingRoom, Color(0xFF00897B), "rooms"),
    HomeOpt("محادثة خاصة", "دردش سرًا", Icons.Default.ChatBubble, Color(0xFFE91E63), "chat/private"),
    HomeOpt("الإعدادات", "تحكم بحسابك", Icons.Default.Settings, Color(0xFF546E7A), "settings"),
    HomeOpt("رصيد النقاط", "أموالي", Icons.Default.Star, Color(0xFFFFA000), "points"),
    HomeOpt("المتجر", "اشترِ وبع", Icons.Default.ShoppingCart, Color(0xFF7B1FA2), "merchant")
)

@Composable
private fun Home(nav: NavHostController) {
    val myUid = FirebaseAuth.getInstance().uid.orEmpty()
    val repo = remember { Repo() }
    var me by remember { mutableStateOf(ChatUser(uid = myUid)) }
    var greet by remember { mutableStateOf("صديقي") }

    LaunchedEffect(myUid) {
        if (myUid.isNotBlank()) {
            repo.user(myUid).get().addOnSuccessListener { s ->
                s.getValue(ChatUser::class.java)?.let {
                    me = it
                    if (it.name.isNotBlank()) greet = it.name
                }
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(me, 56.dp, online = true)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("أهلاً بك، $greet 👋", fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.height(4.dp))
                    Text("ابدأ رحلتك في عالم الدردشة", fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                }
                IconButton({ nav.navigate("editProfile") }) {
                    Icon(Icons.Default.Edit, null,
                        tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        Text("الخيارات السريعة", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            gridItems(homeOptions) { o ->
                HomeCard(o.title, o.subtitle, o.icon, o.color) { nav.navigate(o.route) }
            }
        }
    }
}

@Composable
private fun HomeCard(title: String, subtitle: String,
                     icon: androidx.compose.ui.graphics.vector.ImageVector,
                     color: Color, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(130.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(color),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Column {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

// ═══════════ صورة المستخدم ═══════════
@Composable
private fun UserAvatar(user: ChatUser, size: androidx.compose.ui.unit.Dp,
                       online: Boolean = false) {
    val colors = listOf(Color(0xFF6750A4), Color(0xFF00897B), Color(0xFFE91E63),
        Color(0xFFFFA000), Color(0xFF546E7A), Color(0xFF7B1FA2))
    val fallbackName = user.name.ifBlank { user.uid }.ifBlank { "؟" }
    val bg = colors[(fallbackName.hashCode().absoluteValue) % colors.size]

    Box {
        if (user.photoUrl.isNotBlank()) {
            AsyncImage(
                model = user.photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size).clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape)
            )
        } else {
            Box(Modifier.size(size).clip(CircleShape).background(bg),
                contentAlignment = Alignment.Center) {
                Text(fallbackName.take(1).uppercase(), color = Color.White,
                    fontWeight = FontWeight.Bold, fontSize = (size.value / 2.5f).sp)
            }
        }
        if (online) {
            Box(Modifier.size(size / 4).clip(CircleShape)
                .background(Color(0xFF4CAF50))
                .border(2.dp, Color.White, CircleShape)
                .align(Alignment.BottomEnd))
        }
    }
}

// ═══════════ المستخدمون ═══════════
@Composable
private fun Users(nav: NavHostController) {
    var q by remember { mutableStateOf("") }
    var users by remember { mutableStateOf(listOf<ChatUser>()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        Repo().users().limitToFirst(200).get().addOnSuccessListener { s ->
            users = s.children.mapNotNull { it.getValue(ChatUser::class.java) }
            loading = false
        }.addOnFailureListener { loading = false }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("المستخدمون", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(q, { q = it }, Modifier.fillMaxWidth(),
            placeholder = { Text("ابحث عن مستخدم...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (q.isNotEmpty()) IconButton({ q = "" }) {
                    Icon(Icons.Default.Clear, null)
                }
            },
            singleLine = true, shape = RoundedCornerShape(28.dp))
        Spacer(Modifier.height(12.dp))

        val filtered = users.filter {
            it.name.contains(q, true) || it.uid.contains(q, true)
        }
        when {
            loading -> LoadingBox()
            filtered.isEmpty() -> EmptyState(Icons.Default.PersonOff, "لا يوجد مستخدمون",
                if (q.isBlank()) "لم ينضم أحد بعد" else "لا نتائج للبحث")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered) { u -> UserRow(u, nav) }
            }
        }
    }
}

@Composable
private fun UserRow(u: ChatUser, nav: NavHostController) {
    Card(modifier = Modifier.fillMaxWidth(),
        onClick = { nav.navigate("profile/${u.uid}") }) {
        Row(Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            UserAvatar(u, 48.dp, u.online)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(u.name.ifBlank { "مستخدم" },
                    fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(if (u.online) "متصل الآن" else "غير متصل", fontSize = 12.sp,
                    color = if (u.online) Color(0xFF4CAF50) else Color.Gray)
            }
            IconButton({ nav.navigate("chat/${u.uid}") }) {
                Icon(Icons.Default.ChatBubble, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// ═══════════ الغرف ═══════════
@Composable
private fun Rooms(nav: NavHostController) {
    val repo = remember { RoomRepo() }
    var rooms by remember { mutableStateOf(listOf<ChatRoomModel>()) }
    var showCreate by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        repo.list().limitToFirst(100).get().addOnSuccessListener { s ->
            rooms = s.children.mapNotNull { it.getValue(ChatRoomModel::class.java) }
            loading = false
        }.addOnFailureListener { loading = false }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("الغرف", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Button({ showCreate = true }, shape = RoundedCornerShape(24.dp)) {
                Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("إنشاء")
            }
        }
        if (error.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text(error, Modifier.padding(10.dp), fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        when {
            loading -> LoadingBox()
            rooms.isEmpty() -> EmptyState(Icons.Default.MeetingRoom, "لا توجد غرف",
                "أنشئ أول غرفة دردشة الآن!")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(rooms) { r -> RoomCard(r, nav, repo) { error = it } }
            }
        }
    }

    if (showCreate) {
        CreateRoomDialog({ showCreate = false }) { n, t ->
            repo.create(n, t) { id, e ->
                showCreate = false
                if (id != null) nav.navigate("room/$id") else error = e.orEmpty()
            }
        }
    }
}

@Composable
private fun RoomCard(r: ChatRoomModel, nav: NavHostController,
                     repo: RoomRepo, onError: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).clip(CircleShape)
                    .background(Color(0xFF00897B).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.MeetingRoom, null, tint = Color(0xFF00897B))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(r.name.ifBlank { "غرفة بدون اسم" },
                        fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (r.topic.isNotBlank())
                        Text(r.topic, fontSize = 12.sp, color = Color.Gray)
                }
                AssistChip(onClick = {}, label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.People, null, Modifier.size(14.dp),
                            tint = Color(0xFF00897B))
                        Spacer(Modifier.width(4.dp))
                        Text("${r.memberCount}", fontSize = 12.sp)
                    }
                })
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button({
                    repo.join(r.roomId) { ok, e ->
                        if (ok) nav.navigate("room/${r.roomId}") else onError(e.orEmpty())
                    }
                }, Modifier.weight(1f), shape = RoundedCornerShape(20.dp)) {
                    Icon(Icons.Default.Login, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("دخول")
                }
                OutlinedButton({ nav.navigate("room/${r.roomId}") },
                    shape = RoundedCornerShape(20.dp)) {
                    Icon(Icons.Default.Info, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("التفاصيل")
                }
            }
        }
    }
}

@Composable
private fun CreateRoomDialog(close: () -> Unit, create: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = close,
        icon = { Icon(Icons.Default.AddCircle, null,
            tint = MaterialTheme.colorScheme.primary) },
        title = { Text("إنشاء غرفة دردشة", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(),
                    label = { Text("اسم الغرفة") },
                    leadingIcon = { Icon(Icons.Default.Tag, null) }, singleLine = true)
                OutlinedTextField(topic, { topic = it }, Modifier.fillMaxWidth(),
                    label = { Text("الموضوع") },
                    leadingIcon = { Icon(Icons.Default.Subject, null) }, singleLine = true)
            }
        },
        confirmButton = {
            Button({ if (name.trim().isNotEmpty()) create(name, topic) },
                enabled = name.trim().isNotEmpty()) { Text("إنشاء") }
        },
        dismissButton = { TextButton(close) { Text("إلغاء") } }
    )
}

// ═══════════ شاشة تعديل الملف الشخصي ═══════════
@Composable
private fun EditProfileScreen(nav: NavHostController) {
    val repo = remember { Repo() }
    val myUid = FirebaseAuth.getInstance().uid.orEmpty()

    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var themeColor by remember { mutableStateOf("#6750A4") }
    var photoUrl by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var msg by remember { mutableStateOf("") }
    var uploadingPhoto by remember { mutableStateOf(false) }

    val colorOptions = listOf(
        "#6750A4", "#00897B", "#E91E63",
        "#FFA000", "#546E7A", "#7B1FA2",
        "#1976D2", "#D32F2F"
    )

    LaunchedEffect(myUid) {
        if (myUid.isBlank()) return@LaunchedEffect
        repo.user(myUid).get().addOnSuccessListener { s ->
            s.getValue(ChatUser::class.java)?.let {
                name = it.name
                bio = it.bio
                country = it.country
                themeColor = it.themeColor.ifBlank { "#6750A4" }
                photoUrl = it.photoUrl
            }
            loading = false
        }.addOnFailureListener { loading = false }
    }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            uploadingPhoto = true
            repo.uploadAvatar(myUid, it) { ok, url ->
                uploadingPhoto = false
                if (ok && url != null) {
                    photoUrl = url
                    msg = "✅ تم تحديث الصورة"
                } else {
                    msg = "فشل رفع الصورة (قد يحتاج Firebase Storage إلى ترقية)"
                }
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        Surface(Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer) {
            Row(Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                IconButton({ nav.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Text("تعديل الملف الشخصي", fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        if (loading) {
            LoadingBox()
            return@Column
        }

        LazyColumn(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        if (uploadingPhoto) {
                            Box(Modifier.size(120.dp).clip(CircleShape)
                                .background(Color.LightGray),
                                contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else {
                            UserAvatar(
                                ChatUser(uid = myUid, name = name, photoUrl = photoUrl),
                                120.dp
                            )
                        }
                        Box(
                            Modifier.size(36.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable { photoPicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, null,
                                tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("اضغط على الكاميرا لتغيير الصورة",
                        fontSize = 12.sp, color = Color.Gray)
                }
            }

            item {
                OutlinedTextField(
                    name, { name = it }, Modifier.fillMaxWidth(),
                    label = { Text("الاسم الكامل") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    bio, { bio = it }, Modifier.fillMaxWidth(),
                    label = { Text("نبذة عنك") },
                    placeholder = { Text("اكتب شيئًا عن نفسك...") },
                    leadingIcon = { Icon(Icons.Default.Description, null) },
                    minLines = 3, maxLines = 5
                )
            }

            item {
                OutlinedTextField(
                    country, { country = it }, Modifier.fillMaxWidth(),
                    label = { Text("الدولة") },
                    leadingIcon = { Icon(Icons.Default.Public, null) },
                    singleLine = true
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Palette, null,
                                tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("لون الملف الشخصي",
                                fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            colorOptions.take(4).forEach { hex ->
                                ColorCircle(hex, themeColor) { themeColor = hex }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            colorOptions.drop(4).forEach { hex ->
                                ColorCircle(hex, themeColor) { themeColor = hex }
                            }
                        }
                    }
                }
            }

            if (msg.isNotBlank()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Text(msg, Modifier.padding(12.dp), fontSize = 13.sp)
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        if (name.trim().isBlank()) {
                            msg = "الاسم مطلوب"; return@Button
                        }
                        saving = true
                        repo.updateProfile(name.trim(), bio.trim(),
                            country.trim(), themeColor) { ok, e ->
                            saving = false
                            msg = if (ok) "✅ تم حفظ التغييرات بنجاح"
                                else "فشل الحفظ: ${e ?: "خطأ"}"
                        }
                    },
                    Modifier.fillMaxWidth().height(52.dp),
                    enabled = !saving
                ) {
                    if (saving) {
                        CircularProgressIndicator(Modifier.size(20.dp),
                            color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    } else {
                        Icon(Icons.Default.Save, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("حفظ التغييرات", fontSize = 16.sp,
                        fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ColorCircle(hex: String, selected: String, onClick: () -> Unit) {
    val color = try { Color(android.graphics.Color.parseColor(hex)) }
        catch (_: Exception) { Color.Gray }
    val isSelected = hex == selected
    Box(
        Modifier.size(52.dp).clip(CircleShape).background(color)
            .border(
                width = if (isSelected) 4.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface
                    else Color.Transparent,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(Icons.Default.Check, null, tint = Color.White,
                modifier = Modifier.size(24.dp))
        }
    }
}

// ═══════════ الملف الشخصي ═══════════
@Composable
private fun Profile(nav: NavHostController, uid: String) {
    val repo = remember { RelationshipRepo() }
    val mainRepo = remember { Repo() }
    val me = FirebaseAuth.getInstance().uid.orEmpty()
    var user by remember { mutableStateOf(ChatUser(uid = uid)) }
    var following by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var comments by remember { mutableStateOf(listOf<Comment>()) }

    LaunchedEffect(uid) {
        mainRepo.observeProfile(uid) { user = it }
        repo.comments(uid).limitToLast(50).get().addOnSuccessListener { s ->
            comments = s.children.mapNotNull { it.getValue(Comment::class.java) }
                .sortedByDescending { it.timestamp }
        }
        if (uid != me) repo.observeFollowing(uid) { following = it }
    }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Column(Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally) {
                UserAvatar(user, 100
