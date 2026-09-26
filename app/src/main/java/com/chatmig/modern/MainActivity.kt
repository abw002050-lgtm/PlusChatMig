 @file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)

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

    private var pendingPeer by mutableStateOf<String?>(null)

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        pendingPeer = intent.getStringExtra("chatPeer")
        setContent { HomeApp(pendingPeer) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingPeer = intent.getStringExtra("chatPeer")
    }
}

data class Dest(val route: String, val title: String,
                val icon: androidx.compose.ui.graphics.vector.ImageVector)

val dests = listOf(
    Dest("home", "الرئيسية", Icons.Default.Home),
    Dest("friends", "الأصدقاء", Icons.Default.People),
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
        if (!openPeer.isNullOrBlank() && openPeer.isNotBlank()) {
            // Small delay to let NavHost initialize
            kotlinx.coroutines.delay(300)
            try {
                nav.navigate("chat/$openPeer") {
                    launchSingleTop = true
                }
            } catch (_: Exception) {
                // Ignore navigation errors
            }
        }
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
            composable("friends") { FriendsScreen(nav) }
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
            composable("leaderboard") { LeaderboardScreen(nav) }
        }
    }
}
// ═══════════ الصفحة الرئيسية ═══════════
data class HomeOpt(val title: String, val subtitle: String,
                   val icon: androidx.compose.ui.graphics.vector.ImageVector,
                   val color: Color, val route: String)

val homeOptions = listOf(
    HomeOpt("الأصدقاء", "تواصل منظّم", Icons.Default.People, Color(0xFF6750A4), "friends"),
    HomeOpt("الغرف", "دردشة جماعية", Icons.Default.MeetingRoom, Color(0xFF00897B), "rooms"),
    HomeOpt("محادثة خاصة", "دردش سرًا", Icons.Default.ChatBubble, Color(0xFFE91E63), "chat/private"),
    HomeOpt("الإعدادات", "تحكم بحسابك", Icons.Default.Settings, Color(0xFF546E7A), "settings"),
    HomeOpt("رصيد النقاط", "أموالي", Icons.Default.Star, Color(0xFFFFA000), "points"),
    HomeOpt("المتجر", "اشترِ وبع", Icons.Default.ShoppingCart, Color(0xFF7B1FA2), "merchant"),
    HomeOpt("المتصدرون", "الأعلى نقاطًا", Icons.Default.EmojiEvents, Color(0xFFFFD700), "leaderboard"),
    HomeOpt("النقاط", "تحويل وأكثر", Icons.Default.Send, Color(0xFF2196F3), "points")
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
                UserAvatar(user, 100.dp, user.online)
                Spacer(Modifier.height(12.dp))
                Text(user.name.ifBlank { "مستخدم" },
                    fontSize = 24.sp, fontWeight = FontWeight.Bold)
                if (user.bio.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Card(modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Text(user.bio, Modifier.padding(12.dp),
                            fontSize = 13.sp, textAlign = TextAlign.Center)
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row {
                    if (user.country.isNotBlank())
                        AssistChip(onClick = {}, label = {
                            Row {
                                Icon(Icons.Default.Public, null, Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(user.country, fontSize = 11.sp)
                            }
                        })
                    Spacer(Modifier.width(6.dp))
                    AssistChip(onClick = {}, label = {
                        Text("المستوى ${user.level}", fontSize = 11.sp)
                    })
                }
            }
        }

        if (uid == me) {
            item {
                Button({ nav.navigate("editProfile") },
                    Modifier.fillMaxWidth().height(48.dp)) {
                    Icon(Icons.Default.Edit, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("تعديل ملفي الشخصي", fontSize = 15.sp,
                        fontWeight = FontWeight.Bold)
                }
            }
        }

        if (uid != me) {
            item {
                Row(Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button({
                        repo.follow(uid, !following) { ok, e ->
                            if (ok) following = !following else error = e.orEmpty()
                        }
                    }, Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (following) Color.Gray
                            else MaterialTheme.colorScheme.primary)) {
                        Icon(if (following) Icons.Default.PersonRemove
                            else Icons.Default.PersonAdd, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (following) "إلغاء المتابعة" else "متابعة")
                    }
                    OutlinedButton({ nav.navigate("chat/$uid") }, Modifier.weight(1f)) {
                        Icon(Icons.Default.ChatBubble, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("دردشة")
                    }
                }
            }
        }

        if (error.isNotBlank()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(error, Modifier.padding(10.dp), fontSize = 13.sp)
                }
            }
        }

        item {
            Text("التعليقات (${comments.size})",
                fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        if (uid == me) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(comment, { comment = it }, Modifier.weight(1f),
                            placeholder = { Text("اكتب تعليقًا...") },
                            singleLine = true, shape = RoundedCornerShape(24.dp))
                        IconButton({
                            if (comment.trim().isNotEmpty()) {
                                repo.addComment(uid, comment) { ok, e ->
                                    if (ok) {
                                        comment = ""
                                        repo.comments(uid).get().addOnSuccessListener { s ->
                                            comments = s.children
                                                .mapNotNull { it.getValue(Comment::class.java) }
                                                .sortedByDescending { it.timestamp }
                                        }
                                    } else error = e.orEmpty()
                                }
                            }
                        }) {
                            Icon(Icons.Default.Send, null,
                                tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        if (comments.isEmpty()) {
            item { EmptyState(Icons.Default.Comment, "لا توجد تعليقات", "كن أول من يعلّق!") }
        } else {
            items(comments) { c -> CommentCard(c) }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton({ nav.navigate("comments") }, Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Comment, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("كل التعليقات")
                }
                OutlinedButton({ nav.navigate("settings") }, Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Settings, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("الإعدادات")
                }
                if (uid == me) {
                    Button({
                        mainRepo.setOnline(me, false)
                        FirebaseAuth.getInstance().signOut()
                        nav.navigate("home") { popUpTo("home") { inclusive = true } }
                    }, Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error)) {
                        Icon(Icons.Default.Logout, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("تسجيل الخروج")
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentCard(c: Comment) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp)) {
            UserAvatar(ChatUser(uid = c.authorId, name = c.authorName,
                photoUrl = c.authorPhoto), 36.dp)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(c.authorName.ifBlank { "مستخدم" },
                    fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(Modifier.height(2.dp))
                Text(c.text, fontSize = 14.sp)
            }
        }
    }
}

// ═══════════ الدردشة الخاصة ═══════════
@Composable
private fun Chat(peer: String, nav: NavHostController) {
    val ctx = LocalContext.current
    val repo = remember { Repo() }
    var text by remember { mutableStateOf("") }
    var msgs by remember { mutableStateOf(listOf<ChatMessage>()) }
    var error by remember { mutableStateOf("") }
    var blocked by remember { mutableStateOf(false) }
    var showEmoji by remember { mutableStateOf(false) }
    var recording by remember { mutableStateOf(false) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audioFile by remember { mutableStateOf<File?>(null) }
    var peerUser by remember { mutableStateOf(ChatUser(uid = peer)) }
    var peerTyping by remember { mutableStateOf(false) }
    var replyTo by remember { mutableStateOf<ChatMessage?>(null) }
    var showDeleteDialog by remember { mutableStateOf<ChatMessage?>(null) }

    LaunchedEffect(peer) {
        if (peer != "private" && peer.isNotBlank()) {
            repo.user(peer).get().addOnSuccessListener { s ->
                s.getValue(ChatUser::class.java)?.let { peerUser = it }
            }
        }
    }

    val pick = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            repo.uploadMedia(peer, it, "image", replyTo = replyTo) { ok, e ->
                if (!ok) error = e.orEmpty() else replyTo = null
            }
        }
    }
    val mic = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val f = File(ctx.cacheDir, "chat_${System.currentTimeMillis()}.m4a")
            val r = MediaRecorder(ctx)
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setOutputFile(f.absolutePath)
            r.prepare(); r.start()
            recorder = r; audioFile = f; recording = true
        }
    }

    DisposableEffect(peer) {
        val l = repo.observeMessages(peer, { msgs = it }, { error = it })
        onDispose { repo.removeListener(peer, l) }
    }

    DisposableEffect(peer) {
        val l = repo.observeTyping(peer) { peerTyping = it }
        onDispose { repo.removeTypingListener(peer, l) }
    }

    LaunchedEffect(peerTyping) {
        if (peerTyping) {
            kotlinx.coroutines.delay(6500)
            peerTyping = false
        }
    }

    LaunchedEffect(text) {
        if (peer == "private" || text.isBlank()) return@LaunchedEffect
        repo.setTyping(peer, true)
        kotlinx.coroutines.delay(2500)
        repo.setTyping(peer, false)
    }

    LaunchedEffect(peer) {
        if (peer != "private") repo.isBlocked(peer) { blocked = it }
    }

    Column(Modifier.fillMaxSize()) {
        Surface(Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                IconButton({ nav.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                UserAvatar(
                    if (peer == "private") ChatUser(uid = "private", name = "P")
                    else peerUser,
                    40.dp, peerUser.online && !peerTyping
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (peer == "private") "محادثة خاصة"
                        else peerUser.name.ifBlank { peer },
                        fontWeight = FontWeight.Bold, fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        when {
                            peerTyping -> "✏️ يكتب الآن..."
                            peerUser.online -> "متصل الآن"
                            else -> "غير متصل"
                        },
                        fontSize = 11.sp,
                        color = if (peerTyping) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        fontWeight = if (peerTyping) FontWeight.Bold
                            else FontWeight.Normal
                    )
                }
                if (peer != "private") {
                    IconButton({ repo.setBlocked(peer, !blocked) { blocked = !blocked } }) {
                        Icon(if (blocked) Icons.Default.LockOpen else Icons.Default.Block,
                            null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }

        if (error.isNotBlank()) {
            Card(modifier = Modifier.fillMaxWidth().padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text(error, Modifier.padding(8.dp), fontSize = 12.sp)
            }
        }

        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 8.dp)) {
            if (msgs.isEmpty()) {
                item { EmptyState(Icons.Default.ChatBubbleOutline, "لا توجد رسائل",
                    "ابدأ المحادثة الآن!") }
            }
            itemsIndexed(msgs) { _, m ->
                MessageBubble(
                    m = m,
                    onReply = { replyTo = it },
                    onLongPress = { showDeleteDialog = it }
                )
            }

            if (peerTyping) {
                item {
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(peerUser, 28.dp)
                        Spacer(Modifier.width(6.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Text("يكتب", fontSize = 13.sp, color = Color.Gray)
                                Spacer(Modifier.width(4.dp))
                                Text("•••", fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        if (showEmoji) {
            EmojiPanel { asset ->
                repo.sendEmoji(peer, asset, replyTo = replyTo) { ok, e ->
                    if (!ok) error = e.orEmpty() else replyTo = null
                }
                showEmoji = false
            }
        }

        replyTo?.let { rm ->
            Surface(Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant) {
                Row(Modifier.fillMaxWidth().padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(4.dp).height(40.dp)
                        .background(MaterialTheme.colorScheme.primary))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("الرد على ${rm.senderName.ifBlank { "مستخدم" }}",
                            fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary)
                        Text(
                            when (rm.type) {
                                "image" -> "📷 صورة"
                                "audio" -> "🎤 رسالة صوتية"
                                "emoji" -> "😊 إيموجي"
                                else -> rm.text
                            },
                            fontSize = 12.sp, color = Color.Gray,
                            maxLines = 1
                        )
                    }
                    IconButton({ replyTo = null }) {
                        Icon(Icons.Default.Close, null,
                            tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp) {
            Row(Modifier.fillMaxWidth().padding(6.dp),
                verticalAlignment = Alignment.CenterVertically) {
                IconButton({ pick.launch("image/*") }) {
                    Icon(Icons.Default.Image, null, tint = MaterialTheme.colorScheme.primary)
                }
                IconButton({ showEmoji = !showEmoji }) {
                    Icon(Icons.Default.EmojiEmotions, null,
                        tint = MaterialTheme.colorScheme.primary)
                }
                IconButton({
                    if (recording) {
                        try { recorder?.stop(); recorder?.release() } catch (_: Exception) {}
                        recording = false
                        audioFile?.let {
                            repo.uploadMedia(peer, Uri.fromFile(it), "audio",
                                replyTo = replyTo) { ok, e ->
                                if (!ok) error = e.orEmpty() else replyTo = null
                            }
                        }
                    } else {
                        if (ContextCompat.checkSelfPermission(ctx,
                                Manifest.permission.RECORD_AUDIO) ==
                            PackageManager.PERMISSION_GRANTED) {
                            val f = File(ctx.cacheDir, "chat_${System.currentTimeMillis()}.m4a")
                            val r = MediaRecorder(ctx)
                            r.setAudioSource(MediaRecorder.AudioSource.MIC)
                            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                            r.setOutputFile(f.absolutePath)
                            r.prepare(); r.start()
                            recorder = r; audioFile = f; recording = true
                        } else mic.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }) {
                    Icon(if (recording) Icons.Default.Stop else Icons.Default.Mic, null,
                        tint = if (recording) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary)
                }
                OutlinedTextField(text, { text = it }, Modifier.weight(1f),
                    placeholder = { Text("اكتب رسالة...") }, maxLines = 4,
                    shape = RoundedCornerShape(24.dp))
                IconButton({
                    repo.sendText(peer, text, replyTo = replyTo) { ok, e ->
                        if (ok) {
                            text = ""
                            replyTo = null
                            if (peer != "private") repo.setTyping(peer, false)
                        } else error = e.orEmpty()
                    }
                }) {
                    Icon(Icons.Default.Send, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    showDeleteDialog?.let { m ->
        val myUid = FirebaseAuth.getInstance().uid.orEmpty()
        val canDeleteForEveryone = m.senderId == myUid && !m.deletedForEveryone
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = { Icon(Icons.Default.Delete, null,
                tint = MaterialTheme.colorScheme.error) },
            title = { Text("حذف الرسالة", fontWeight = FontWeight.Bold) },
            text = { Text("كيف تريد حذف هذه الرسالة؟") },
            confirmButton = {
                if (canDeleteForEveryone) {
                    Button(
                        onClick = {
                            repo.deleteForEveryone(peer, m.id)
                            showDeleteDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("للجميع") }
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        repo.deleteForMe(peer, m.id)
                        showDeleteDialog = null
                    }) { Text("لي فقط") }
                    TextButton(onClick = { showDeleteDialog = null }) { Text("إلغاء") }
                }
            }
        )
    }
}
// ═══════════ فقاعة الرسالة ═══════════
@Composable
private fun MessageBubble(
    m: ChatMessage,
    onReply: ((ChatMessage) -> Unit)? = null,
    onLongPress: ((ChatMessage) -> Unit)? = null
) {
    val myUid = FirebaseAuth.getInstance().uid
    val isMe = m.senderId == myUid
    val fmt = SimpleDateFormat("hh:mm a", Locale("ar"))

    Row(
        Modifier.fillMaxWidth().combinedClickable(
            onClick = { onReply?.invoke(m) },
            onLongClick = { onLongPress?.invoke(m) }
        ),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isMe) {
            UserAvatar(
                ChatUser(uid = m.senderId, name = m.senderName,
                    photoUrl = m.senderPhoto),
                32.dp
            )
            Spacer(Modifier.width(6.dp))
        }
        Card(modifier = Modifier.widthIn(max = 270.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isMe) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(10.dp)) {
                if (m.replyToId.isNotBlank()) {
                    Box(
                        Modifier.fillMaxWidth().padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                    ) {
                        Row(Modifier.padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.width(3.dp).height(28.dp)
                                .background(if (isMe) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.primary))
                            Spacer(Modifier.width(6.dp))
                            Column {
                                Text(m.replyToSender,
                                    fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                    color = if (isMe) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.primary)
                                Text(m.replyToText,
                                    fontSize = 11.sp, maxLines = 1,
                                    color = if (isMe)
                                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                    else Color.Gray)
                            }
                        }
                    }
                }

                if (!isMe && m.senderName.isNotBlank() && !m.deletedForEveryone) {
                    Text(m.senderName, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                }

                if (m.deletedForEveryone) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Block, null,
                            tint = if (isMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            else Color.Gray,
                            modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("تم حذف هذه الرسالة",
                            color = if (isMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            else Color.Gray,
                            fontSize = 13.sp, fontStyle = FontStyle.Italic)
                    }
                } else {
                    when (m.type) {
                        "image" -> AsyncImage(model = m.mediaUrl, contentDescription = null,
                            modifier = Modifier.size(180.dp).clip(RoundedCornerShape(8.dp)))
                        "audio" -> AudioBubble(m.mediaUrl)
                        "emoji" -> AsyncImage(
                            model = "file:///android_asset/emoji/${m.assetName}",
                            contentDescription = null, modifier = Modifier.size(46.dp))
                        else -> Text(m.text,
                            color = if (isMe) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp)
                    }
                }

                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(fmt.format(Date(m.timestamp)), fontSize = 10.sp,
                        color = if (isMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        else Color.Gray)
                    if (isMe && !m.deletedForEveryone) {
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            if (m.status == "read") Icons.Default.DoneAll else Icons.Default.Done,
                            null,
                            tint = if (m.status == "read") Color(0xFF4FC3F7)
                            else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioBubble(url: String) {
    var playing by remember { mutableStateOf(false) }
    Button({
        val p = android.media.MediaPlayer()
        p.setDataSource(url)
        p.setOnCompletionListener { playing = false; p.release() }
        p.prepareAsync()
        p.setOnPreparedListener { it.start(); playing = true }
    }) {
        Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
            null, Modifier.size(18.dp))
        Spacer(Modifier.width(4.dp))
        Text(if (playing) "جاري التشغيل..." else "تشغيل الصوت")
    }
}

@Composable
private fun EmojiPanel(onPick: (String) -> Unit) {
    val ctx = LocalContext.current
    val names = remember { ctx.assets.list("emoji")?.take(80) ?: emptyList() }
    LazyVerticalGrid(columns = GridCells.Fixed(8),
        modifier = Modifier.fillMaxWidth().height(220.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)) {
        items(names.size) { i ->
            IconButton({ onPick(names[i]) }) {
                AsyncImage(model = "file:///android_asset/emoji/${names[i]}",
                    contentDescription = null, modifier = Modifier.size(32.dp))
            }
        }
    }
}
// ═══════════ دردشة الغرفة ═══════════
@Composable
private fun RoomChat(roomId: String, nav: NavHostController) {
    val repo = remember { RoomRepo() }
    val me = FirebaseAuth.getInstance().uid.orEmpty()
    var room by remember { mutableStateOf(ChatRoomModel(roomId = roomId)) }
    var members by remember { mutableStateOf(listOf<RoomMember>()) }
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var text by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var isOwner by remember { mutableStateOf(false) }
    var showMembers by remember { mutableStateOf(true) }
    var replyTo by remember { mutableStateOf<ChatMessage?>(null) }
    var showDeleteDialog by remember { mutableStateOf<ChatMessage?>(null) }

    LaunchedEffect(roomId) {
        repo.room(roomId).get().addOnSuccessListener {
            room = it.getValue(ChatRoomModel::class.java) ?: room
        }
        repo.members(roomId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                members = s.children.mapNotNull { it.getValue(RoomMember::class.java) }
                isOwner = members.any { it.uid == me && it.role == "owner" }
            }
            override fun onCancelled(e: DatabaseError) {}
        })
    }

    DisposableEffect(roomId) {
        val l = object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                messages = s.children.mapNotNull { x ->
                    val delFor = x.child("deletedFor").children
                        .mapNotNull { it.getValue(String::class.java) }
                    if (delFor.contains(me)) return@mapNotNull null
                    ChatMessage(
                        x.key.orEmpty(),
                        x.child("senderId").getValue(String::class.java).orEmpty(),
                        x.child("message_name").getValue(String::class.java).orEmpty(),
                        x.child("senderPhoto").getValue(String::class.java).orEmpty(),
                        x.child("message").getValue(String::class.java).orEmpty(),
                        "text", "", "",
                        x.child("message_time").getValue(Long::class.java) ?: 0L,
                        x.child("status").getValue(String::class.java) ?: "sent",
                        "",
                        x.child("replyToId").getValue(String::class.java).orEmpty(),
                        x.child("replyToText").getValue(String::class.java).orEmpty(),
                        x.child("replyToSender").getValue(String::class.java).orEmpty(),
                        x.child("deletedForEveryone").getValue(Boolean::class.java) ?: false,
                        delFor
                    )
                }.sortedBy { it.timestamp }
            }
            override fun onCancelled(e: DatabaseError) { error = e.message.orEmpty() }
        }
        repo.messages(roomId).addValueEventListener(l)
        onDispose { repo.messages(roomId).removeEventListener(l) }
    }

    Column(Modifier.fillMaxSize()) {
        Surface(Modifier.fillMaxWidth(), color = Color(0xFF00897B)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically) {
                IconButton({ nav.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                }
                Column(Modifier.weight(1f)) {
                    Text(room.name.ifBlank { "غرفة" }, fontWeight = FontWeight.Bold,
                        color = Color.White, fontSize = 16.sp)
                    Text("${members.size} أعضاء • ${room.topic.ifBlank { "بدون موضوع" }}",
                        color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                }
                IconButton({ showMembers = !showMembers }) {
                    Icon(Icons.Default.People, null, tint = Color.White)
                }
            }
        }

        if (showMembers && members.isNotEmpty()) {
            Surface(Modifier.fillMaxWidth(),
                color = Color(0xFF00897B).copy(alpha = 0.15f)) {
                LazyColumn(
                    Modifier.fillMaxWidth().heightIn(max = 180.dp)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.People, null,
                                tint = Color(0xFF00897B), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("الأعضاء (${members.size})",
                                fontWeight = FontWeight.Bold, fontSize = 13.sp,
                                color = Color(0xFF00897B))
                        }
                    }
                    items(members) { m ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(
                                ChatUser(uid = m.uid, name = m.name,
                                    photoUrl = m.photoUrl),
                                36.dp, online = false
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(m.name.ifBlank { "مستخدم" },
                                        fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    if (m.role == "owner") {
                                        Spacer(Modifier.width(4.dp))
                                        Icon(Icons.Default.Star, null,
                                            tint = Color(0xFFFFA000),
                                            modifier = Modifier.size(14.dp))
                                    }
                                }
                                Text(
                                    when (m.role) {
                                        "owner" -> "المالك"
                                        "moderator" -> "مشرف"
                                        else -> "عضو"
                                    },
                                    fontSize = 11.sp, color = Color.Gray
                                )
                            }
                            if (isOwner && m.role != "owner" && m.uid != me) {
                                IconButton(
                                    { repo.kick(roomId, m.uid) {} },
                                    Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.RemoveCircle, null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        if (error.isNotBlank()) {
            Card(modifier = Modifier.fillMaxWidth().padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text(error, Modifier.padding(8.dp), fontSize = 12.sp)
            }
        }

        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 8.dp)) {
            if (messages.isEmpty()) {
                item { EmptyState(Icons.Default.Forum, "لا توجد رسائل",
                    "كن أول من يكتب في الغرفة!") }
            }
            items(messages) { m ->
                MessageBubble(
                    m = m,
                    onReply = { replyTo = it },
                    onLongPress = { showDeleteDialog = it }
                )
            }
        }

        replyTo?.let { rm ->
            Surface(Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant) {
                Row(Modifier.fillMaxWidth().padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(4.dp).height(40.dp)
                        .background(Color(0xFF00897B)))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("الرد على ${rm.senderName.ifBlank { "مستخدم" }}",
                            fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            color = Color(0xFF00897B))
                        Text(rm.text, fontSize = 12.sp, color = Color.Gray,
                            maxLines = 1)
                    }
                    IconButton({ replyTo = null }) {
                        Icon(Icons.Default.Close, null,
                            tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        Surface(tonalElevation = 3.dp) {
            Row(Modifier.fillMaxWidth().padding(6.dp),
                verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(text, { text = it }, Modifier.weight(1f),
                    placeholder = { Text("اكتب رسالة...") }, maxLines = 4,
                    shape = RoundedCornerShape(24.dp))
                IconButton({
                    val uid = FirebaseAuth.getInstance().uid
                    when {
                        uid == null -> error = "يجب تسجيل الدخول"
                        text.trim().isNotEmpty() -> {
                            val k = repo.messages(roomId).push().key
                            if (k != null) {
                                val v = hashMapOf<String, Any>(
                                    "message_id" to k,
                                    "message" to text.trim(),
                                    "message_name" to (FirebaseAuth.getInstance()
                                        .currentUser?.displayName ?: "مستخدم"),
                                    "message_type" to "text",
                                    "message_time" to ServerValue.TIMESTAMP,
                                    "senderId" to uid,
                                    "status" to "sent"
                                )
                                replyTo?.let {
                                    v["replyToId"] = it.id
                                    v["replyToText"] = it.text
                                    v["replyToSender"] = it.senderName.ifBlank { "مستخدم" }
                                }
                                repo.messages(roomId).child(k).setValue(v)
                                    .addOnFailureListener {
                                        error = it.localizedMessage.orEmpty()
                                    }
                            }
                            text = ""
                            replyTo = null
                        }
                    }
                }) { Icon(Icons.Default.Send, null, tint = Color(0xFF00897B)) }
            }
        }

        Row(Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton({ repo.leave(roomId) { nav.popBackStack() } },
                Modifier.weight(1f)) {
                Icon(Icons.Default.ExitToApp, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("مغادرة")
            }
            if (isOwner) {
                Button({ repo.raise(roomId) { ok -> if (!ok) error = "فشل" } },
                    Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000))) {
                    Icon(Icons.Default.Star, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("ترقية")
                }
            }
        }
    }

    showDeleteDialog?.let { m ->
        val myUid = FirebaseAuth.getInstance().uid.orEmpty()
        val canDeleteForEveryone = m.senderId == myUid && !m.deletedForEveryone

        fun deleteRoomForMe() {
            val ref = repo.messages(roomId).child(m.id).child("deletedFor")
            ref.get().addOnSuccessListener { s ->
                val list = s.children.mapNotNull { it.getValue(String::class.java) }.toMutableList()
                if (!list.contains(myUid)) list.add(myUid)
                ref.setValue(list)
            }.addOnFailureListener { ref.setValue(listOf(myUid)) }
            showDeleteDialog = null
        }
        fun deleteRoomForEveryone() {
            repo.messages(roomId).child(m.id)
                .child("deletedForEveryone").setValue(true)
            showDeleteDialog = null
        }

        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = { Icon(Icons.Default.Delete, null,
                tint = MaterialTheme.colorScheme.error) },
            title = { Text("حذف الرسالة", fontWeight = FontWeight.Bold) },
            text = { Text("كيف تريد حذف هذه الرسالة؟") },
            confirmButton = {
                if (canDeleteForEveryone) {
                    Button(
                        onClick = { deleteRoomForEveryone() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("للجميع") }
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { deleteRoomForMe() }) { Text("لي فقط") }
                    TextButton(onClick = { showDeleteDialog = null }) { Text("إلغاء") }
                }
            }
        )
    }
}
// ═══════════ النقاط ═══════════
@Composable
private fun Points(nav: NavHostController) {
    val repo = remember { PointsRepo() }
    val me = FirebaseAuth.getInstance().uid.orEmpty()
    var points by remember { mutableStateOf(0L) }
var message by remember { mutableStateOf("") }
var receiver by remember { mutableStateOf("") }
var amount by remember { mutableStateOf("") }

val rewardsRepo = remember { RewardsRepo() }
var rewardState by remember { mutableStateOf(RewardsRepo.RewardState()) }

DisposableEffect(me) {
    if (me.isBlank()) onDispose {}
    else {
        val l = repo.observeBalance({ points = it }, { message = it })
        val lr = rewardsRepo.observeState { rewardState = it }
        onDispose {
            repo.removeBalanceListener(l)
            rewardsRepo.removeStateListener(lr)
        }
    }
}
LazyColumn(Modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)) {
    item {
        Card(modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFA000))) {
            Column(Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color.White,
                        modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("رصيدك الحالي", color = Color.White, fontSize = 16.sp,
                        fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                Text("$points", color = Color.White, fontSize = 44.sp,
                    fontWeight = FontWeight.Bold)
                Text("نقطة", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
            }
        }
    }

    // ═══════════ 🎁 بطاقة المكافآت اليومية ═══════════
    item {
        Card(modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎁", fontSize = 24.sp)
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text("المكافآت اليومية",
                            fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            if (rewardState.currentStreak > 0)
                                "🔥 سلسلة: ${rewardState.currentStreak}/${RewardsRepo.STREAK_TARGET} أيام"
                            else "ابدأ سلسلتك اليوم!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))

                if (rewardState.canClaimDaily) {
                    Button(
                        onClick = {
                            rewardsRepo.claimDaily { result ->
                                message = result.message
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("🎁 استلم +${RewardsRepo.DAILY_REWARD} نقطة",
                            fontWeight = FontWeight.Bold)
                    }
                } else {
                    val hours = rewardState.nextClaimInMs / (60 * 60 * 1000)
                    val minutes = (rewardState.nextClaimInMs / (60 * 1000)) % 60
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = false
                    ) {
                        Text("⏰ متاح بعد $hours س و $minutes د")
                    }
                }

if (!rewardState.welcomeClaimed) {
    Spacer(Modifier.height(8.dp))
    Button(
        onClick = {
            rewardsRepo.claimWelcome { result ->
                message = result.message
            }
        },
        modifier = Modifier.fillMaxWidth().height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4CAF50))
    ) {
        Text("🎉 استلم مكافأة الترحيب (+${RewardsRepo.WELCOME_BONUS})",
            fontWeight = FontWeight.Bold)
    }
}

if (!rewardState.profileClaimed) {
    Spacer(Modifier.height(8.dp))
    Button(
        onClick = {
            rewardsRepo.claimProfileComplete { result ->
                message = result.message
            }
        },
        modifier = Modifier.fillMaxWidth().height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2196F3))
    ) {
        Text("📝 أكمل ملفك → +${RewardsRepo.PROFILE_BONUS} نقطة",
            fontWeight = FontWeight.Bold)
    }
}
        }
    }
}

item {
    Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Send, null,
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("تحويل النقاط", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(receiver, { receiver = it }, Modifier.fillMaxWidth(),
                        label = { Text("معرف المستلم") },
                        leadingIcon = { Icon(Icons.Default.Person, null) }, singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(amount, { amount = it.filter(Char::isDigit) },
                        Modifier.fillMaxWidth(),
                        label = { Text("عدد النقاط") },
                        leadingIcon = { Icon(Icons.Default.Star, null) }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    Spacer(Modifier.height(12.dp))
                    Button({
                        val n = amount.toLongOrNull()
                        if (n == null || n <= 0) message = "أدخل عددًا صحيحًا"
                        else repo.sendPoints(receiver.trim(), n) { ok, e ->
                            message = if (ok) "تم تحويل $n نقطة بنجاح ✅" else e.orEmpty()
                            if (ok) { receiver = ""; amount = "" }
                        }
                    }, Modifier.fillMaxWidth().height(48.dp)) {
                        Icon(Icons.Default.Send, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("إرسال النقاط")
                    }
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("ملاحظات", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("• لا يمكنك الإرسال لنفسك", fontSize = 12.sp)
                    Text("• يجب أن تملك 3000 نقطة كحد أدنى", fontSize = 12.sp)
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button({ nav.navigate("merchant") }, Modifier.weight(1f)) {
                    Icon(Icons.Default.ShoppingCart, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("المتجر")
                }
                OutlinedButton({ nav.navigate("transactions") }, Modifier.weight(1f)) {
                    Icon(Icons.Default.History, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("العمليات")
                }
            }
        }
        if (message.isNotBlank()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Text(message, Modifier.padding(12.dp), fontSize = 13.sp)
                }
            }
        }
    }
}

// ═══════════ المتجر ═══════════
@Composable
private fun MerchantScreen(nav: NavHostController) {
    val repo = remember { PointsRepo() }
    val me = FirebaseAuth.getInstance().uid.orEmpty()
    var items by remember { mutableStateOf(listOf<Merchant>()) }
    var showAdd by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Merchant?>(null) }
    var error by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        val l = object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                items = s.children.mapNotNull { it.getValue(Merchant::class.java) }
            }
            override fun onCancelled(e: DatabaseError) { error = e.message.orEmpty() }
        }
        repo.merchantList().addValueEventListener(l)
        onDispose { repo.merchantRef().removeEventListener(l) }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("المتجر", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Button({ showAdd = true }, shape = RoundedCornerShape(24.dp)) {
                Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("إضافة")
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
        if (items.isEmpty()) {
            EmptyState(Icons.Default.ShoppingBag, "لا يوجد منتجات", "أضف أول منتج للبيع!")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(items) { m ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            Row(Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top) {
                                Column(Modifier.weight(1f)) {
                                    Text(m.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    if (m.description.isNotBlank())
                                        Text(m.description, fontSize = 12.sp, color = Color.Gray)
                                }
                                AssistChip(onClick = {}, label = {
                                    Text(if (m.active) "متوفر" else "معطل", fontSize = 11.sp)
                                }, leadingIcon = {
                                    Box(Modifier.size(8.dp).clip(CircleShape)
                                        .background(if (m.active) Color(0xFF4CAF50)
                                        else Color.Gray))
                                })
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = Color(0xFFFFA000),
                                    modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("${m.price} نقطة", fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFA000))
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (m.ownerId != me && m.active) {
                                    Button({ selected = m }, Modifier.weight(1f)) {
                                        Icon(Icons.Default.ShoppingCart, null,
                                            Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("شراء")
                                    }
                                }
                                if (m.ownerId == me) {
                                    OutlinedButton({
                                        repo.setMerchantActive(m.merchantId, !m.active) { ok, e ->
                                            if (!ok) error = e.orEmpty()
                                        }
                                    }, Modifier.weight(1f)) {
                                        Text(if (m.active) "تعطيل" else "تفعيل")
                                    }
                                    OutlinedButton({
                                        repo.deleteMerchant(m.merchantId) { ok, e ->
                                            if (!ok) error = e.orEmpty()
                                        }
                                    }, Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error)) {
                                        Text("حذف")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton({ nav.navigate("transactions") }, Modifier.fillMaxWidth()) {
            Icon(Icons.Default.History, null, Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("سجل العمليات")
        }
    }
    if (showAdd) {
        AddMerchantDialog({ showAdd = false }) { n, d, p ->
            repo.addMerchant(n, d, p) { ok, e ->
                showAdd = false
                if (!ok) error = e.orEmpty()
            }
        }
    }
    selected?.let { m ->
        AlertDialog(onDismissRequest = { selected = null },
            icon = { Icon(Icons.Default.ShoppingCart, null,
                tint = MaterialTheme.colorScheme.primary) },
            title = { Text("تأكيد الشراء") },
            text = { Text("هل تريد شراء \"${m.name}\" بـ ${m.price} نقطة؟") },
            confirmButton = {
                Button({
                    repo.purchaseMerchant(m) { ok, e ->
                        error = if (ok) "تم الشراء بنجاح ✅" else e.orEmpty()
                        if (ok) selected = null
                    }
                }) { Text("تأكيد") }
            },
            dismissButton = { TextButton({ selected = null }) { Text("إلغاء") } }
        )
    }
}

// ═══════════ العمليات ═══════════
@Composable
private fun Transactions() {
    val repo = remember { PointsRepo() }
    val me = FirebaseAuth.getInstance().uid.orEmpty()
    var list by remember { mutableStateOf(listOf<PointTransaction>()) }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(me) {
        if (me.isNotBlank()) {
            repo.transactions().get().addOnSuccessListener { s ->
                list = s.children.mapNotNull { it.getValue(PointTransaction::class.java) }
                    .sortedByDescending { it.createdAt }
                loading = false
            }.addOnFailureListener {
                error = it.localizedMessage.orEmpty(); loading = false
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("عمليات النقاط", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        when {
            loading -> LoadingBox()
            error.isNotBlank() -> Card(modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text(error, Modifier.padding(12.dp))
            }
            list.isEmpty() -> EmptyState(Icons.Default.Receipt, "لا توجد عمليات",
                "لم تقم بأي عملية بعد")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(list) { t ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(40.dp).clip(CircleShape).background(
                                if (t.type == "merchant_purchase")
                                    Color(0xFFFFA000).copy(alpha = 0.2f)
                                else Color(0xFF6750A4).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center) {
                                Icon(if (t.type == "merchant_purchase")
                                    Icons.Default.ShoppingCart else Icons.Default.Send,
                                    null, tint = if (t.type == "merchant_purchase")
                                        Color(0xFFFFA000) else Color(0xFF6750A4))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(if (t.type == "merchant_purchase")
                                    "شراء من المتجر" else "تحويل نقاط",
                                    fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (t.merchantName.isNotBlank())
                                    Text("المنتج: ${t.merchantName}",
                                        fontSize = 12.sp, color = Color.Gray)
                                Text("الحالة: ${t.status}",
                                    fontSize = 11.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${t.amount}", fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFA000), fontSize = 18.sp)
                                Text("نقطة", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════ إضافة منتج ═══════════
@Composable
private fun AddMerchantDialog(close: () -> Unit,
                              create: (String, String, Long) -> Unit) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = close,
        icon = { Icon(Icons.Default.AddShoppingCart, null,
            tint = MaterialTheme.colorScheme.primary) },
        title = { Text("إضافة منتج", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(),
                    label = { Text("اسم المنتج") },
                    leadingIcon = { Icon(Icons.Default.LocalOffer, null) },
                    singleLine = true)
                OutlinedTextField(desc, { desc = it }, Modifier.fillMaxWidth(),
                    label = { Text("الوصف") },
                    leadingIcon = { Icon(Icons.Default.Subject, null) },
                    singleLine = true)
                OutlinedTextField(price, { price = it.filter(Char::isDigit) },
                    Modifier.fillMaxWidth(),
                    label = { Text("السعر (نقاط)") },
                    leadingIcon = { Icon(Icons.Default.Star, null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = {
            Button({
                val p = price.toLongOrNull()
                if (name.trim().isNotEmpty() && p != null && p > 0) create(name, desc, p)
            }, enabled = name.trim().isNotEmpty() && price.isNotBlank()) { Text("حفظ") }
        },
        dismissButton = { TextButton(close) { Text("إلغاء") } }
    )
}

// ═══════════ التعليقات ═══════════
@Composable
private fun Comments() {
    val repo = remember { RelationshipRepo() }
    val uid = FirebaseAuth.getInstance().uid.orEmpty()
    var text by remember { mutableStateOf("") }
    var list by remember { mutableStateOf(listOf<Comment>()) }
    var msg by remember { mutableStateOf("") }

    LaunchedEffect(uid) {
        if (uid.isNotBlank()) {
            repo.comments(uid).limitToLast(100).get().addOnSuccessListener { s ->
                list = s.children.mapNotNull { it.getValue(Comment::class.java) }
                    .sortedByDescending { it.timestamp }
            }
        }
    }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("كل التعليقات", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        if (list.isEmpty()) {
            EmptyState(Icons.Default.Comment, "لا توجد تعليقات", "شارك رأيك الآن!")
        } else {
            LazyColumn(Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(list) { c -> CommentCard(c) }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(text, { text = it }, Modifier.weight(1f),
                placeholder = { Text("اكتب تعليقًا...") },
                shape = RoundedCornerShape(24.dp), singleLine = true)
            Spacer(Modifier.width(8.dp))
            IconButton({
                if (text.trim().isNotEmpty()) {
                    repo.addComment(uid, text) { ok, e ->
                        if (ok) { text = ""; msg = "تم إرسال التعليق ✅" }
                        else msg = e.orEmpty()
                    }
                }
            }) {
                Icon(Icons.Default.Send, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        if (msg.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(msg, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}

// ═══════════ الإعدادات ═══════════
@Composable
private fun Settings() {
    val repo = remember { RelationshipRepo() }
    val nrepo = remember { NotificationRepo() }
    var blocked by remember { mutableStateOf(listOf<String>()) }
    var notifications by remember { mutableStateOf(listOf<AppNotification>()) }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        repo.blockList().get().addOnSuccessListener { s ->
            blocked = s.children.filter { it.getValue(Boolean::class.java) == true }
                .map { it.key.orEmpty() }
        }
        nrepo.inbox().limitToLast(50).get().addOnSuccessListener { s ->
            notifications = s.children.mapNotNull { it.getValue(AppNotification::class.java) }
                .sortedByDescending { it.timestamp }
        }
    }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("الإعدادات", fontSize = 24.sp, fontWeight = FontWeight.Bold) }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, null,
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("الإشعارات (${notifications.size})",
                            fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    if (notifications.isEmpty()) {
                        Text("لا توجد إشعارات", fontSize = 13.sp, color = Color.Gray)
                    } else {
                        notifications.take(5).forEach { n ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(8.dp).clip(CircleShape)
                                    .background(if (n.read) Color.Gray
                                    else Color(0xFF6750A4)))
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(n.title, fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp)
                                    Text(n.body, fontSize = 12.sp, color = Color.Gray)
                                }
                                if (!n.read) {
                                    TextButton({
                                        nrepo.markRead(n.id)
                                        notifications = notifications.map {
                                            if (it.id == n.id) it.copy(read = true) else it
                                        }
                                    }) { Text("قراءة", fontSize = 11.sp) }
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Block, null,
                            tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text("قائمة الحظر (${blocked.size})",
                            fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    if (blocked.isEmpty()) {
                        Text("لا يوجد محظورون", fontSize = 13.sp, color = Color.Gray)
                    } else {
                        blocked.forEach { id ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                UserAvatar(ChatUser(uid = id, name = id), 32.dp)
                                Spacer(Modifier.width(10.dp))
                                Text(id, Modifier.weight(1f), fontSize = 13.sp)
                                TextButton({
                                    repo.blockList().child(id).setValue(false)
                                    blocked = blocked.filterNot { it == id }
                                    message = "تم إلغاء الحظر"
                                }) { Text("إلغاء", fontSize = 12.sp) }
                            }
                        }
                    }
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Icon(Icons.Default.Tune, null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.height(6.dp))
                    Text("عام", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(10.dp))
                    SettingRow(Icons.Default.Language, "اللغة", "العربية")
                    SettingRow(Icons.Default.Info, "الإصدار", "1.0.0-v27")
                    SettingRow(Icons.Default.Person, "الملف الشخصي", "")
                }
            }
        }
        if (message.isNotBlank()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Text(message, Modifier.padding(12.dp), fontSize = 13.sp)
                }
            }
        }
    }
}
// ═══════════ لوحة المتصدرين ═══════════
@Composable
private fun LeaderboardScreen(nav: NavHostController) {
    val repo = remember { LeaderboardRepo() }
    val me = FirebaseAuth.getInstance().uid.orEmpty()
    var entries by remember { mutableStateOf(listOf<LeaderboardRepo.LeaderEntry>()) }
    var loading by remember { mutableStateOf(true) }
    var myRank by remember { mutableStateOf(0) }
    var myPoints by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        repo.fetchTop(20) { list ->
            entries = list
            loading = false
        }
        repo.fetchMyRank { rank, pts ->
            myRank = rank
            myPoints = pts
        }
    }

    Column(Modifier.fillMaxSize()) {
        // Header
        Surface(Modifier.fillMaxWidth(), color = Color(0xFFFFA000)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton({ nav.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                }
                Text("🏆 المتصدرون", fontWeight = FontWeight.Bold,
                    color = Color.White, fontSize = 20.sp)
            }
        }

        // My rank card
        if (myRank > 0) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF))
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (myRank) {
                            1 -> "🥇"
                            2 -> "🥈"
                            3 -> "🥉"
                            else -> "#$myRank"
                        },
                        fontSize = 32.sp
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("ترتيبك", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("أنت", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("$myPoints", fontWeight = FontWeight.Bold,
                            fontSize = 22.sp, color = Color(0xFFFFA000))
                        Text("نقطة", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }

        // List
        when {
            loading -> LoadingBox()
            entries.isEmpty() -> EmptyState(
                Icons.Default.EmojiEvents,
                "لا يوجد متصدرون بعد",
                "ابدأ بكسب النقاط لتظهر هنا!"
            )
            else -> LazyColumn(
                Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(entries) { entry ->
                    LeaderCard(entry, entry.uid == me) {
                        if (entry.uid != me) nav.navigate("profile/${entry.uid}")
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun LeaderCard(
    entry: LeaderboardRepo.LeaderEntry,
    isMe: Boolean,
    onClick: () -> Unit
) {
    val bgColor = when {
        isMe -> Color(0xFFEADDFF)
        entry.rank == 1 -> Color(0xFFFFF8E1)
        entry.rank == 2 -> Color(0xFFF5F5F5)
        entry.rank == 3 -> Color(0xFFFFF3E0)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        onClick = onClick
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank
            Box(
                Modifier.size(44.dp).clip(CircleShape)
                    .background(
                        when (entry.rank) {
                            1 -> Color(0xFFFFD700)  // Gold
                            2 -> Color(0xFFC0C0C0)  // Silver
                            3 -> Color(0xFFCD7F32)  // Bronze
                            else -> Color.Gray.copy(alpha = 0.3f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (entry.rank) {
                        1 -> "🥇"
                        2 -> "🥈"
                        3 -> "🥉"
                        else -> "${entry.rank}"
                    },
                    fontSize = if (entry.rank <= 3) 22.sp else 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(Modifier.width(12.dp))

            // Avatar
            UserAvatar(
                ChatUser(uid = entry.uid, name = entry.name,
                    photoUrl = entry.photoUrl),
                44.dp
            )

            Spacer(Modifier.width(12.dp))

            // Name
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        entry.name.ifBlank { "مستخدم" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    if (isMe) {
                        Spacer(Modifier.width(4.dp))
                        Text("(أنت)", fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold)
                    }
                }
                Text("المرتبة ${entry.rank}",
                    fontSize = 11.sp, color = Color.Gray)
            }

            // Points
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null,
                        tint = Color(0xFFFFA000),
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${entry.points}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFFFFA000))
                }
                Text("نقطة", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

// ═══════════ شاشة الأصدقاء ═══════════
@Composable
private fun FriendsScreen(nav: NavHostController) {
    val repo = remember { FriendsRepo() }
    var friends by remember { mutableStateOf(listOf<Friendship>()) }
    var incoming by remember { mutableStateOf(listOf<FriendRequest>()) }
    var outgoing by remember { mutableStateOf(listOf<FriendRequest>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showRequestsDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val l1 = repo.observeFriends { friends = it }
        val l2 = repo.observeIncomingRequests { incoming = it }
        val l3 = repo.observeOutgoingRequests { outgoing = it }
        onDispose {
            repo.removeFriendsListener(l1)
            repo.removeIncomingListener(l2)
            repo.removeOutgoingListener(l3)
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("الأصدقاء", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Row {
                BadgedBox(
                    badge = {
                        if (incoming.isNotEmpty()) {
                            Badge { Text("${incoming.size}") }
                        }
                    }
                ) {
                    IconButton({ showRequestsDialog = true }) {
                        Icon(Icons.Default.Mail, null,
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }
                IconButton({ showAddDialog = true }) {
                    Icon(Icons.Default.PersonAdd, null,
                        tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (incoming.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                onClick = { showRequestsDialog = true }
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Mail, null,
                        tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(10.dp))
                    Text("لديك ${incoming.size} طلب صداقة",
                        Modifier.weight(1f), fontWeight = FontWeight.Bold,
                        fontSize = 14.sp)
                    Icon(Icons.Default.ArrowForward, null,
                        tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        if (friends.isEmpty()) {
            EmptyState(
                Icons.Default.PeopleOutline,
                "لا يوجد أصدقاء بعد",
                "اضغط على ➕ لإضافة صديق"
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(friends) { f ->
                    FriendCard(f, nav, repo)
                }
            }
        }
    }

    if (showAddDialog) {
        AddFriendDialog({ showAddDialog = false }, nav)
    }

    if (showRequestsDialog) {
        FriendRequestsDialog(
            incoming = incoming,
            outgoing = outgoing,
            repo = repo,
            onClose = { showRequestsDialog = false }
        )
    }
}

@Composable
private fun FriendCard(f: Friendship, nav: NavHostController, repo: FriendsRepo) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { nav.navigate("chat/${f.friendId}") }
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                ChatUser(uid = f.friendId, name = f.friendName, photoUrl = f.friendPhoto),
                48.dp
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(f.friendName.ifBlank { "صديق" },
                    fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("اضغط للدردشة", fontSize = 12.sp, color = Color.Gray)
            }
            Box {
                IconButton({ showMenu = true }) {
                    Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("دردشة") },
                        leadingIcon = {
                            Icon(Icons.Default.ChatBubble, null,
                                tint = MaterialTheme.colorScheme.primary)
                        },
                        onClick = {
                            showMenu = false
                            nav.navigate("chat/${f.friendId}")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("عرض الملف") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = Color.Gray) },
                        onClick = {
                            showMenu = false
                            nav.navigate("profile/${f.friendId}")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("إزالة الصديق") },
                        leadingIcon = {
                            Icon(Icons.Default.PersonRemove, null,
                                tint = MaterialTheme.colorScheme.error)
                        },
                        onClick = {
                            showMenu = false
                            repo.removeFriend(f.friendId) { }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AddFriendDialog(close: () -> Unit, nav: NavHostController) {
    val repo = remember { FriendsRepo() }
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf(listOf<ChatUser>()) }
    var loading by remember { mutableStateOf(false) }
    var msg by remember { mutableStateOf("") }

    LaunchedEffect(query) {
        if (query.length < 2) { results = emptyList(); return@LaunchedEffect }
        loading = true
        kotlinx.coroutines.delay(400)
        repo.searchUsers(query) { list ->
            results = list
            loading = false
        }
    }

    AlertDialog(
        onDismissRequest = close,
        icon = { Icon(Icons.Default.PersonAdd, null,
            tint = MaterialTheme.colorScheme.primary) },
        title = { Text("إضافة صديق", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    query, { query = it; msg = "" },
                    Modifier.fillMaxWidth(),
                    placeholder = { Text("اسم المستخدم أو البريد") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (loading) {
                            CircularProgressIndicator(Modifier.size(18.dp),
                                strokeWidth = 2.dp)
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp)
                )
                if (msg.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(msg, fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(8.dp))
                if (results.isEmpty() && query.length >= 2 && !loading) {
                    Text("لا توجد نتائج", fontSize = 13.sp, color = Color.Gray)
                } else {
                    LazyColumn(
                        Modifier.fillMaxWidth().heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(results) { u ->
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatar(u, 40.dp, u.online)
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(u.name.ifBlank { "مستخدم" },
                                        fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    if (u.online)
                                        Text("متصل الآن", fontSize = 11.sp,
                                            color = Color(0xFF4CAF50))
                                }
                                IconButton({
                                    repo.sendFriendRequest(u) { ok, e ->
                                        msg = if (ok) "✅ تم إرسال الطلب"
                                            else "خطأ: ${e ?: "غير معروف"}"
                                    }
                                }) {
                                    Icon(Icons.Default.PersonAdd, null,
                                        tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton({
                                    close()
                                    nav.navigate("chat/${u.uid}")
                                }) {
                                    Icon(Icons.Default.ChatBubble, null,
                                        tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(close) { Text("إغلاق") } }
    )
}

@Composable
private fun FriendRequestsDialog(
    incoming: List<FriendRequest>,
    outgoing: List<FriendRequest>,
    repo: FriendsRepo,
    onClose: () -> Unit
) {
    var tab by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onClose,
        icon = { Icon(Icons.Default.Mail, null,
            tint = MaterialTheme.colorScheme.primary) },
        title = { Text("طلبات الصداقة", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                TabRow(selectedTabIndex = tab) {
                    Tab(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        text = { Text("واردة (${incoming.size})") }
                    )
                    Tab(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        text = { Text("صادرة (${outgoing.size})") }
                    )
                }
                Spacer(Modifier.height(8.dp))

                val list = if (tab == 0) incoming else outgoing
                if (list.isEmpty()) {
                    Text(
                        if (tab == 0) "لا توجد طلبات واردة"
                        else "لا توجد طلبات صادرة",
                        fontSize = 13.sp, color = Color.Gray,
                        modifier = Modifier.padding(20.dp)
                    )
                } else {
                    LazyColumn(
                        Modifier.fillMaxWidth().heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(list) { req ->
                            Card(Modifier.fillMaxWidth()) {
                                Row(
                                    Modifier.fillMaxWidth().padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    UserAvatar(
                                        ChatUser(
                                            uid = if (tab == 0) req.fromId else req.toId,
                                            name = req.fromName
                                        ),
                                        40.dp
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(req.fromName.ifBlank { "مستخدم" },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp)
                                        Text(
                                            if (tab == 0) "يريد أن يصادقك"
                                            else "بانتظار الرد",
                                            fontSize = 11.sp, color = Color.Gray
                                        )
                                    }
                                    if (tab == 0) {
                                        IconButton({
                                            repo.acceptRequest(req) { }
                                        }) {
                                            Icon(Icons.Default.Check, null,
                                                tint = Color(0xFF4CAF50))
                                        }
                                        IconButton({
                                            repo.rejectRequest(req) { }
                                        }) {
                                            Icon(Icons.Default.Close, null,  
                                                tint = MaterialTheme.colorScheme.error)
                                        }
                                    } else {
                                        TextButton({
                                            repo.cancelOutgoing(req) {}
                                        }) { Text("إلغاء", fontSize = 12.sp) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text("إغلاق") } }
    )
}

// ═══════════ مكونات مساعدة UI ═══════════
@Composable
private fun SettingRow(icon: androidx.compose.ui.graphics.vector.ImageVector,
                       title: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(title, fontSize = 14.sp)
        }
        if (value.isNotBlank()) {
            Text(value, fontSize = 13.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun LoadingBox() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState(icon: androidx.compose.ui.graphics.vector.ImageVector,
                       title: String, subtitle: String) {
    Column(
        Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, null, Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.5f))
        Spacer(Modifier.height(12.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, fontSize = 12.sp,
            color = Color.Gray.copy(alpha = 0.8f), textAlign = TextAlign.Center)
    }
}
