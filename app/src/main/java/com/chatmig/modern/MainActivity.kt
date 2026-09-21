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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
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

// ═══════════════════════════════════════════════════════
// الشاشة الرئيسية
// ═══════════════════════════════════════════════════════

class MainActivity : ComponentActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(
                primary = Color(0xFF6750A4),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFEADDFF),
                secondary = Color(0xFF625B71)
            )) {
                LoginScreen()
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// شاشة تسجيل الدخول
// ═══════════════════════════════════════════════════════

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

    LaunchedEffect(Unit) {
        if (a.currentUser != null) {
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
            // شعار التطبيق
            Box(
                Modifier.size(100.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ChatBubble,
                    contentDescription = null,
                    Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "شات ميج 33",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "تواصل، دردش، وشارك",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(30.dp))

            OutlinedTextField(
                email, { email = it },
                Modifier.fillMaxWidth(),
                label = { Text("البريد الإلكتروني") },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                pass, { pass = it },
                Modifier.fillMaxWidth(),
                label = { Text("كلمة المرور") },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    IconButton({ showPass = !showPass }) {
                        Icon(
                            if (showPass) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            null
                        )
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
                        msg = "أدخل البريد وكلمة المرور"
                        return@Button
                    }
                    loading = true
                    msg = ""
                    if (reg) {
                        a.createUserWithEmailAndPassword(email.trim(), pass)
                            .addOnSuccessListener {
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
                Modifier.fillMaxWidth().height(52.dp),
                enabled = !loading
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    if (reg) "إنشاء الحساب" else "تسجيل الدخول",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            TextButton({ reg = !reg; msg = "" }) {
                Text(
                    if (reg) "لديك حساب؟ سجل الدخول"
                    else "ليس لديك حساب؟ أنشئ واحدًا"
                )
            }
            if (msg.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        msg,
                        Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// النشاط الرئيسي بعد الدخول
// ═══════════════════════════════════════════════════════

class HomeActivity : ComponentActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContent { HomeApp(intent.getStringExtra("chatPeer")) }
    }
}

data class Dest(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

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
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                dests.forEach { d ->
                    NavigationBarItem(
                        selected == d.route,
                        {
                            selected = d.route
                            nav.navigate(d.route) { popUpTo("home") }
                        },
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
            composable("profile") { Profile(nav, FirebaseAuth.getInstance().uid.orEmpty()) }
            composable("profile/{id}") { b ->
                Profile(nav, b.arguments?.getString("id").orEmpty())
            }
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

// ═══════════════════════════════════════════════════════
// الصفحة الرئيسية
// ═══════════════════════════════════════════════════════

@Composable
private fun Home(nav: NavHostController) {
    val user = FirebaseAuth.getInstance().currentUser
    val displayName = user?.displayName ?: user?.email?.substringBefore("@") ?: "صديقي"

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // بطاقة ترحيب
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    "أهلاً بك، $displayName 👋",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "ابدأ رحلتك في عالم الدردشة",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        Text("الخيارات السريعة", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))

        LazyVerticalGrid(
            GridCells.Fixed(2),
            Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            gridItems(homeOptions.size) { i ->
                val opt = homeOptions[i]
                HomeCard(
                    title = opt.first,
                    subtitle = opt.second,
                    icon = opt.third,
                    color = opt.fourth
                ) { nav.navigate(opt.fifth) }
            }
        }
    }
}

val homeOptions = listOf(
    Quadruple("المستخدمون", "اكتشف أصدقاء", Icons.Default.People, Color(0xFF6750A4), "users"),
    Quadruple("الغرف", "دردشة جماعية", Icons.Default.MeetingRoom, Color(0xFF00897B), "rooms"),
    Quadruple("محادثة خاصة", "دردش سرًا", Icons.Default.ChatBubble, Color(0xFFE91E63), "chat/private"),
    Quadruple("الإعدادات", "تحكم بحسابك", Icons.Default.Settings, Color(0xFF546E7A), "settings"),
    Quadruple("رصيد النقاط", "أموالي", Icons.Default.Star, Color(0xFFFFA000), "points"),
    Quadruple("المتجر", "اشترِ وبع", Icons.Default.ShoppingCart, Color(0xFF7B1FA2), "merchant")
)

data class Quadruple<A, B, C, D>(
    val first: A, val second: B, val third: C, val fourth: D, val fifth: String
)

@Composable
private fun HomeCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        Modifier.fillMaxWidth().height(130.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Column {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// المستخدمون
// ═══════════════════════════════════════════════════════

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
        OutlinedTextField(
            q, { q = it },
            Modifier.fillMaxWidth(),
            placeholder = { Text("ابحث عن مستخدم...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (q.isNotEmpty()) IconButton({ q = "" }) {
                    Icon(Icons.Default.Clear, null)
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp)
        )
        Spacer(Modifier.height(12.dp))

        val filtered = users.filter {
            it.name.contains(q, true) || it.uid.contains(q, true)
        }

        when {
            loading -> LoadingBox()
            filtered.isEmpty() -> EmptyState(
                Icons.Default.PersonOff, "لا يوجد مستخدمون",
                if (q.isBlank()) "لم ينضم أحد بعد" else "لا نتائج للبحث"
            )
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered) { u ->
                    UserRow(u, nav)
                }
            }
        }
    }
}

@Composable
private fun UserRow(u: ChatUser, nav: NavHostController) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(name = u.name.ifBlank { u.uid }, size = 48.dp, online = u.online)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    u.name.ifBlank { "مستخدم" },
                    fontWeight = FontWeight.Bold, fontSize = 15.sp
                )
                Text(
                    if (u.online) "متصل الآن" else "غير متصل",
                    fontSize = 12.sp,
                    color = if (u.online) Color(0xFF4CAF50) else Color.Gray
                )
            }
            IconButton({ nav.navigate("profile/${u.uid}") }) {
                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
            }
            IconButton({ nav.navigate("chat/${u.uid}") }) {
                Icon(Icons.Default.ChatBubble, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun UserAvatar(name: String, size: androidx.compose.ui.unit.Dp, online: Boolean = false) {
    val colors = listOf(
        Color(0xFF6750A4), Color(0xFF00897B), Color(0xFFE91E63),
        Color(0xFFFFA000), Color(0xFF546E7A), Color(0xFF7B1FA2)
    )
    val bg = colors[(name.hashCode().absoluteValue) % colors.size]
    Box {
        Box(
            Modifier.size(size).clip(CircleShape).background(bg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                name.take(1).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value / 2.5f).sp
            )
        }
        if (online) {
            Box(
                Modifier.size(size / 4).clip(CircleShape)
                    .background(Color(0xFF4CAF50))
                    .align(Alignment.BottomEnd)
                    .border(2.dp, Color.White, CircleShape)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// الغرف
// ═══════════════════════════════════════════════════════

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
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("الغرف", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Button({ showCreate = true }, shape = RoundedCornerShape(24.dp)) {
                Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("إنشاء")
            }
        }
        if (error.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) { Text(error, Modifier.padding(10.dp), fontSize = 13.sp) }
        }
        Spacer(Modifier.height(12.dp))

        when {
            loading -> LoadingBox()
            rooms.isEmpty() -> EmptyState(
                Icons.Default.MeetingRoom, "لا توجد غرف",
                "أنشئ أول غرفة دردشة الآن!"
            )
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(rooms) { r ->
                    RoomCard(r, nav, repo) { error = it }
                }
            }
        }
    }

    if (showCreate) {
        CreateRoomDialog({ showCreate = false }) { name, topic ->
            repo.create(name, topic) { id, e ->
                showCreate = false
                if (id != null) nav.navigate("room/$id") else error = e.orEmpty()
            }
        }
    }
}

@Composable
private fun RoomCard(
    r: ChatRoomModel, nav: NavHostController,
    repo: RoomRepo, onError: (String) -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(44.dp).clip(CircleShape)
                        .background(Color(0xFF00897B).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MeetingRoom, null, tint = Color(0xFF00897B))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        r.name.ifBlank { "غرفة بدون اسم" },
                        fontWeight = FontWeight.Bold, fontSize = 16.sp
                    )
                    if (r.topic.isNotBlank())
                        Text(r.topic, fontSize = 12.sp, color = Color.Gray)
                }
                // شارة عدد الأعضاء
                AssistChip(
                    onClick = {},
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.People, null,
                                Modifier.size(14.dp), tint = Color(0xFF00897B))
                            Spacer(Modifier.width(4.dp))
                            Text("${r.memberCount}", fontSize = 12.sp)
                        }
                    }
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    {
                        repo.join(r.roomId) { ok, e ->
                            if (ok) nav.navigate("room/${r.roomId}")
                            else onError(e.orEmpty())
                        }
                    },
                    Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Default.Login, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("دخول")
                }
                OutlinedButton(
                    { nav.navigate("room/${r.roomId}") },
                    shape = RoundedCornerShape(20.dp)
                ) {
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
    AlertDialog(
        onDismissRequest = close,
        icon = { Icon(Icons.Default.AddCircle, null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("إنشاء غرفة دردشة", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    name, { name = it },
                    Modifier.fillMaxWidth(),
                    label = { Text("اسم الغرفة") },
                    leadingIcon = { Icon(Icons.Default.Tag, null) },
                    singleLine = true
                )
                OutlinedTextField(
                    topic, { topic = it },
                    Modifier.fillMaxWidth(),
                    label = { Text("الموضوع") },
                    leadingIcon = { Icon(Icons.Default.Subject, null) },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                { if (name.trim().isNotEmpty()) create(name, topic) },
                enabled = name.trim().isNotEmpty()
            ) { Text("إنشاء") }
        },
        dismissButton = { TextButton(close) { Text("إلغاء") } }
    )
}

// ═══════════════════════════════════════════════════════
// الملف الشخصي
// ═══════════════════════════════════════════════════════

@Composable
private fun Profile(nav: NavHostController, uid: String) {
    val repo = remember { RelationshipRepo() }
    val me = FirebaseAuth.getInstance().uid.orEmpty()
    var user by remember { mutableStateOf(ChatUser(uid = uid)) }
    var following by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var comments by remember { mutableStateOf(listOf<Comment>()) }

    LaunchedEffect(uid) {
        repo.profile(uid).get().addOnSuccessListener { s ->
            user = s.getValue(ChatUser::class.java) ?: user
        }
        repo.comments(uid).limitToLast(50).get().addOnSuccessListener { s ->
            comments = s.children.mapNotNull { it.getValue(Comment::class.java) }
                .sortedByDescending { it.timestamp }
        }
        if (uid != me) repo.observeFollowing(uid) { following = it }
    }

    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // رأس الملف
        item {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                UserAvatar(
                    name = user.name.ifBlank { uid },
                    size = 90.dp,
                    online = user.online
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    user.name.ifBlank { "مستخدم" },
                    fontSize = 22.sp, fontWeight = FontWeight.Bold
                )
                Text(
                    if (user.country.isBlank()) "لم تحدد الدولة" else user.country,
                    fontSize = 13.sp, color = Color.Gray
                )
                Spacer(Modifier.height(4.dp))
                AssistChip(
                    onClick = {},
                    label = { Text("المستوى ${user.level}", fontSize = 12.sp) }
                )
            }
        }

        // إحصائيات سريعة (إن كانت لمستخدم آخر)
        if (uid != me) {
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        {
                            repo.follow(uid, !following) { ok, e ->
                                if (ok) following = !following else error = e.orEmpty()
                            }
                        },
                        Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (following) Color.Gray
                                else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            if (following) Icons.Default.PersonRemove
                            else Icons.Default.PersonAdd,
                            null, Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(if (following) "إلغاء المتابعة" else "متابعة")
                    }
                    OutlinedButton(
                        { nav.navigate("chat/$uid") },
                        Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ChatBubble, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("دردشة")
                    }
                }
            }
        }

        if (error.isNotBlank()) {
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) { Text(error, Modifier.padding(10.dp), fontSize = 13.sp) }
            }
        }

        // التعليقات
        item {
            Text(
                "التعليقات (${comments.size})",
                fontSize = 16.sp, fontWeight = FontWeight.Bold
            )
        }

        if (uid == me) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            comment, { comment = it },
                            Modifier.weight(1f),
                            placeholder = { Text("اكتب تعليقًا...") },
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp)
                        )
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
                            Icon(
                                Icons.Default.Send, null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        if (comments.isEmpty()) {
            item {
                EmptyState(
                    Icons.Default.Comment,
                    "لا توجد تعليقات",
                    "كن أول من يعلّق!"
                )
            }
        } else {
            items(comments) { c -> CommentCard(c) }
        }

        // أزرار أسفل الملف
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    { nav.navigate("comments") },
                    Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Comment, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("كل التعليقات")
                }
                OutlinedButton(
                    { nav.navigate("settings") },
                    Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Settings, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("الإعدادات")
                }
                if (uid == me) {
                    Button(
                        {
                            FirebaseAuth.getInstance().signOut()
                            nav.navigate("home") { popUpTo("home") { inclusive = true } }
                        },
                        Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
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
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp)) {
            UserAvatar(name = c.authorName.ifBlank { "؟" }, size = 36.dp)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    c.authorName.ifBlank { "مستخدم" },
                    fontWeight = FontWeight.Bold, fontSize = 13.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(c.text, fontSize = 14.sp)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// المحادثة الخاصة
// ═══════════════════════════════════════════════════════

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

    val pick = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            repo.uploadMedia(peer, it, "image") { ok, e ->
                if (!ok) error = e.orEmpty()
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
    LaunchedEffect(peer) { if (peer != "private") repo.isBlocked(peer) { blocked = it } }

    Column(Modifier.fillMaxSize()) {
        // رأس الدردشة
        Surface(
            Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton({ nav.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                UserAvatar(name = peer, size = 36.dp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (peer == "private") "محادثة خاصة" else peer,
                        fontWeight = FontWeight.Bold, fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "متصل", fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                if (peer != "private") {
                    IconButton({ repo.setBlocked(peer, !blocked) { blocked = !blocked } }) {
                        Icon(
                            if (blocked) Icons.Default.LockOpen else Icons.Default.Block,
                            null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        if (error.isNotBlank()) {
            Card(
                Modifier.fillMaxWidth().padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) { Text(error, Modifier.padding(8.dp), fontSize = 12.sp) }
        }

        // الرسائل
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth()
                .padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            if (msgs.isEmpty()) {
                item {
                    EmptyState(
                        Icons.Default.ChatBubbleOutline,
                        "لا توجد رسائل",
                        "ابدأ المحادثة الآن!"
                    )
                }
            }
            itemsIndexed(msgs) { _, m ->
                MessageBubble(m)
            }
        }

        if (showEmoji) {
            EmojiPanel { asset ->
                repo.sendEmoji(peer, asset) { ok, e -> if (!ok) error = e.orEmpty() }
                showEmoji = false
            }
        }

        // شريط الإدخال
        Surface(
            Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp
        ) {
            Row(
                Modifier.fillMaxWidth().padding(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton({ pick.launch("image/*") }) {
                    Icon(Icons.Default.Image, null, tint = MaterialTheme.colorScheme.primary)
                }
                IconButton({ showEmoji = !showEmoji }) {
                    Icon(Icons.Default.EmojiEmotions, null, tint = MaterialTheme.colorScheme.primary)
                }
                IconButton({
                    if (recording) {
                        try { recorder?.stop(); recorder?.release() } catch (_: Exception) {}
                        recording = false
                        audioFile?.let {
                            repo.uploadMedia(peer, Uri.fromFile(it), "audio") { ok, e ->
                                if (!ok) error = e.orEmpty()
                            }
                        }
                    } else {
                        if (ContextCompat.checkSelfPermission(
                                ctx, Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
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
                    Icon(
                        if (recording) Icons.Default.Stop else Icons.Default.Mic,
                        null,
                        tint = if (recording) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                    )
                }
                OutlinedTextField(
                    text, { text = it },
                    Modifier.weight(1f),
                    placeholder = { Text("اكتب رسالة...") },
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp)
                )
                IconButton({
                    repo.sendText(peer, text) { ok, e ->
                        if (ok) text = "" else error = e.orEmpty()
                    }
                }) {
                    Icon(Icons.Default.Send, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(m: ChatMessage) {
    val myUid = FirebaseAuth.getInstance().uid
    val isMe = m.senderId == myUid
    val fmt = SimpleDateFormat("hh:mm a", Locale("ar"))

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!isMe) {
            UserAvatar(name = m.senderName.ifBlank { "؟" }, size = 30.dp)
            Spacer(Modifier.width(6.dp))
        }
        Card(
            Modifier.widthIn(max = 270.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isMe) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(Modifier.padding(10.dp)) {
                if (!isMe && m.senderName.isNotBlank()) {
                    Text(
                        m.senderName, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                when (m.type) {
                    "image" -> AsyncImage(
                        model = m.mediaUrl, contentDescription = null,
                        modifier = Modifier.size(180.dp).clip(RoundedCornerShape(8.dp))
                    )
                    "audio" -> AudioBubble(m.mediaUrl)
                    "emoji" -> AsyncImage(
                        model = "file:///android_asset/emoji/${m.assetName}",
                        contentDescription = null,
                        modifier = Modifier.size(46.dp)
                    )
                    else -> Text(
                        m.text,
                        color = if (isMe) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 15.sp
                    )
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    fmt.format(Date(m.timestamp)),
                    fontSize = 10.sp,
                    color = if (isMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        else Color.Gray
                )
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
        Icon(
            if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
            null, Modifier.size(18.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(if (playing) "جاري التشغيل..." else "تشغيل الصوت")
    }
}

@Composable
private fun EmojiPanel(onPick: (String) -> Unit) {
    val ctx = LocalContext.current
    val names = remember { ctx.assets.list("emoji")?.take(80) ?: emptyList() }
    LazyVerticalGrid(
        GridCells.Fixed(8),
        Modifier.fillMaxWidth().height(220.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        items(names.size) { i ->
            IconButton({ onPick(names[i]) }) {
                AsyncImage(
                    model = "file:///android_asset/emoji/${names[i]}",
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// دردشة الغرفة
// ═══════════════════════════════════════════════════════

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
    var showMembers by remember { mutableStateOf(false) }

    LaunchedEffect(roomId) {
        repo.room(roomId).get().addOnSuccessListener {
            room = it.getValue(ChatRoomModel::class.java) ?: room
        }
        repo.members(roomId).get().addOnSuccessListener { s ->
            members = s.children.mapNotNull { it.getValue(RoomMember::class.java) }
            isOwner = members.any { it.uid == me && it.role == "owner" }
        }
    }
    DisposableEffect(roomId) {
        val l = object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                messages = s.children.mapNotNull { x ->
                    ChatMessage(
                        x.key.orEmpty(),
                        x.child("senderId").getValue(String::class.java).orEmpty(),
                        x.child("message_name").getValue(String::class.java).orEmpty(),
                        x.child("message").getValue(String::class.java).orEmpty(),
                        "text", "", "",
                        x.child("message_time").getValue(Long::class.java) ?: 0L,
                        x.child("status").getValue(String::class.java) ?: "sent",
                        ""
                    )
                }.sortedBy { it.timestamp }
            }
            override fun onCancelled(e: DatabaseError) { error = e.message.orEmpty() }
        }
        repo.messages(roomId).addValueEventListener(l)
        onDispose { repo.messages(roomId).removeEventListener(l) }
    }

    Column(Modifier.fillMaxSize()) {
        // رأس الغرفة
        Surface(
            Modifier.fillMaxWidth(),
            color = Color(0xFF00897B)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton({ nav.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        room.name.ifBlank { "غرفة" },
                        fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp
                    )
                    Text(
                        "${room.memberCount} أعضاء • ${room.topic.ifBlank { "بدون موضوع" }}",
                        color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp
                    )
                }
                IconButton({ showMembers = true }) {
                    Icon(Icons.Default.People, null, tint = Color.White)
                }
            }
        }

        if (error.isNotBlank()) {
            Card(
                Modifier.fillMaxWidth().padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) { Text(error, Modifier.padding(8.dp), fontSize = 12.sp) }
        }

        LazyColumn(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    EmptyState(
                        Icons.Default.Forum, "لا توجد رسائل",
                        "كن أول من يكتب في الغرفة!"
                    )
                }
            }
            items(messages) { m -> MessageBubble(m) }
        }

        Surface(tonalElevation = 3.dp) {
            Row(
                Modifier.fillMaxWidth().padding(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    text, { text = it },
                    Modifier.weight(1f),
                    placeholder = { Text("اكتب رسالة...") },
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp)
                )
                IconButton({
                    val uid = FirebaseAuth.getInstance().uid
                    when {
                        uid == null -> error = "يجب تسجيل الدخول"
                        text.trim().isNotEmpty() -> {
                            val k = repo.messages(roomId).push().key
                            if (k != null) {
                                repo.messages(roomId).child(k).setValue(
                                    mapOf(
                                        "message_id" to k,
                                        "message" to text.trim(),
                                        "message_name" to (FirebaseAuth.getInstance()
                                            .currentUser?.displayName ?: "مستخدم"),
                                        "message_type" to "text",
                                        "message_time" to ServerValue.TIMESTAMP,
                                        "senderId" to uid,
                                        "status" to "sent"
                                    )
                                ).addOnFailureListener {
                                    error = it.localizedMessage.orEmpty()
                                }
                            }
                            text = ""
                        }
                    }
                }) {
                    Icon(Icons.Default.Send, null, tint = Color(0xFF00897B))
                }
            }
        }

        // أزرار أسفل الغرفة
        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                { repo.leave(roomId) { nav.popBackStack() } },
                Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ExitToApp, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("مغادرة")
            }
            if (isOwner) {
                Button(
                    { repo.raise(roomId) { ok -> if (!ok) error = "فشل" } },
                    Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000))
                ) {
                    Icon(Icons.Default.Star, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("ترقية")
                }
            }
        }
    }

    if (showMembers) {
        RoomMembersDialog(roomId, members, isOwner, repo) { showMembers = false }
    }
}

@Composable
private fun RoomMembersDialog(
    roomId: String, members: List<RoomMember>,
    owner: Boolean, repo: RoomRepo, close: () -> Unit
) {
    AlertDialog(
        onDismissRequest = close,
        icon = { Icon(Icons.Default.People, null, tint = Color(0xFF00897B)) },
        title = { Text("أعضاء الغرفة (${members.size})", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(members) { m ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(name = m.name.ifBlank { m.uid }, size = 36.dp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(m.name.ifBlank { m.uid }, fontWeight = FontWeight.Bold,
                                fontSize = 14.sp)
                            Text(
                                when (m.role) {
                                    "owner" -> "المالك"
                                    "moderator" -> "مشرف"
                                    else -> "عضو"
                                },
                                fontSize = 11.sp, color = Color.Gray
                            )
                        }
                        if (owner && m.role != "owner") {
                            IconButton({ repo.kick(roomId, m.uid) {} }) {
                                Icon(Icons.Default.RemoveCircle, null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(close) { Text("إغلاق") } }
    )
}

// ═══════════════════════════════════════════════════════
// النقاط
// ═══════════════════════════════════════════════════════

@Composable
private fun Points(nav: NavHostController) {
    val repo = remember { PointsRepo() }
    val me = FirebaseAuth.getInstance().uid.orEmpty()
    var points by remember { mutableStateOf(0L) }
    var message by remember { mutableStateOf("") }
    var receiver by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    DisposableEffect(me) {
        if (me.isBlank()) onDispose {}
        else {
            val l = repo.observeBalance({ points = it }, { message = it })
            onDispose { repo.removeBalanceListener(l) }
        }
    }

    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFA000))
            ) {
                Column(Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color.White,
                            modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("رصيدك الحالي", color = Color.White,
                            fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "$points",
                        color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Bold
                    )
                    Text("نقطة", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                }
            }
        }

        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Send, null,
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("تحويل النقاط", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        receiver, { receiver = it },
                        Modifier.fillMaxWidth(),
                        label = { Text("معرف المستلم") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        amount, { amount = it.filter(Char::isDigit) },
                        Modifier.fillMaxWidth(),
                        label = { Text("عدد النقاط") },
                        leadingIcon = { Icon(Icons.Default.Star, null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        {
                            val n = amount.toLongOrNull()
                            if (n == null || n <= 0) message = "أدخل عددًا صحيحًا"
                            else repo.sendPoints(receiver.trim(), n) { ok, e ->
                                message = if (ok) "تم تحويل $n نقطة بنجاح ✅"
                                    else e.orEmpty()
                                if (ok) { receiver = ""; amount = "" }
                            }
                        },
                        Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.Send, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("إرسال النقاط")
                    }
                }
            }
        }

        item {
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
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
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) { Text(message, Modifier.padding(12.dp), fontSize = 13.sp) }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// المتجر
// ═══════════════════════════════════════════════════════

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
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("المتجر", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Button({ showAdd = true }, shape = RoundedCornerShape(24.dp)) {
                Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("إضافة")
            }
        }
        if (error.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) { Text(error, Modifier.padding(10.dp), fontSize = 13.sp) }
        }
        Spacer(Modifier.height(12.dp))

        if (items.isEmpty()) {
            EmptyState(
                Icons.Default.ShoppingBag, "لا يوجد منتجات",
                "أضف أول منتج للبيع!"
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(items) { m ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(m.name, fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp)
                                    if (m.description.isNotBlank())
                                        Text(m.description, fontSize = 12.sp,
                                            color = Color.Gray)
                                }
                                AssistChip(
                                    onClick = {},
                                    label = {
                                        Text(
                                            if (m.active) "متوفر" else "معطل",
                                            fontSize = 11.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Box(
                                            Modifier.size(8.dp).clip(CircleShape)
                                                .background(
                                                    if (m.active) Color(0xFF4CAF50)
                                                    else Color.Gray
                                                )
                                        )
                                    }
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null,
                                    tint = Color(0xFFFFA000),
                                    modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("${m.price} نقطة",
                                    fontWeight = FontWeight.Bold,
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
                                    OutlinedButton(
                                        {
                                            repo.setMerchantActive(m.merchantId, !m.active) { ok, e ->
                                                if (!ok) error = e.orEmpty()
                                            }
                                        }, Modifier.weight(1f)
                                    ) {
                                        Text(if (m.active) "تعطيل" else "تفعيل")
                                    }
                                    OutlinedButton(
                                        {
                                            repo.deleteMerchant(m.merchantId) { ok, e ->
                                                if (!ok) error = e.orEmpty()
                                            }
                                        }, Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) { Text("حذف") }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            { nav.navigate("transactions") },
            Modifier.fillMaxWidth()
        ) {
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
        AlertDialog(
            onDismissRequest = { selected = null },
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

// ═══════════════════════════════════════════════════════
// العمليات
// ═══════════════════════════════════════════════════════

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
            error.isNotBlank() -> Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) { Text(error, Modifier.padding(12.dp)) }
            list.isEmpty() -> EmptyState(
                Icons.Default.Receipt, "لا توجد عمليات",
                "لم تقم بأي عملية بعد"
            )
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(list) { t ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(40.dp).clip(CircleShape).background(
                                    if (t.type == "merchant_purchase")
                                        Color(0xFFFFA000).copy(alpha = 0.2f)
                                    else Color(0xFF6750A4).copy(alpha = 0.2f)
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (t.type == "merchant_purchase")
                                        Icons.Default.ShoppingCart
                                    else Icons.Default.Send,
                                    null,
                                    tint = if (t.type == "merchant_purchase")
                                        Color(0xFFFFA000) else Color(0xFF6750A4)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    if (t.type == "merchant_purchase")
                                        "شراء من المتجر" else "تحويل نقاط",
                                    fontWeight = FontWeight.Bold, fontSize = 14.sp
                                )
                                if (t.merchantName.isNotBlank())
                                    Text("المنتج: ${t.merchantName}",
                                        fontSize = 12.sp, color = Color.Gray)
                                Text("الحالة: ${t.status}",
                                    fontSize = 11.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${t.amount}",
                                    fontWeight = FontWeight.Bold,
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

@Composable
private fun AddMerchantDialog(
    close: () -> Unit,
    create: (String, String, Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = close,
        icon = { Icon(Icons.Default.AddShoppingCart, null,
            tint = MaterialTheme.colorScheme.primary) },
        title = { Text("إضافة منتج", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    name, { name = it }, Modifier.fillMaxWidth(),
                    label = { Text("اسم المنتج") },
                    leadingIcon = { Icon(Icons.Default.LocalOffer, null) },
                    singleLine = true
                )
                OutlinedTextField(
                    desc, { desc = it }, Modifier.fillMaxWidth(),
                    label = { Text("الوصف") },
                    leadingIcon = { Icon(Icons.Default.Subject, null) },
                    singleLine = true
                )
                OutlinedTextField(
                    price, { price = it.filter(Char::isDigit) },
                    Modifier.fillMaxWidth(),
                    label = { Text("السعر (نقاط)") },
                    leadingIcon = { Icon(Icons.Default.Star, null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button({
                val p = price.toLongOrNull()
                if (name.trim().isNotEmpty() && p != null && p > 0) create(name, desc, p)
            }, enabled = name.trim().isNotEmpty() && price.isNotBlank()) {
                Text("حفظ")
            }
        },
        dismissButton = { TextButton(close) { Text("إلغاء") } }
    )
}

// ═══════════════════════════════════════════════════════
// التعليقات
// ═══════════════════════════════════════════════════════

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
            OutlinedTextField(
                text, { text = it }, Modifier.weight(1f),
                placeholder = { Text("اكتب تعليقًا...") },
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            IconButton({
                if (text.trim().isNotEmpty()) {
                    repo.addComment(uid, text) { ok, e ->
                        if (ok) { text = ""; msg = "تم إرسال التعليق ✅" }
                        else msg = e.orEmpty()
                    }
                }
            }) {
                Icon(Icons.Default.Send, null,
                    tint = MaterialTheme.colorScheme.primary)
            }
        }
        if (msg.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(msg, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}

// ═══════════════════════════════════════════════════════
// الإعدادات
// ═══════════════════════════════════════════════════════

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

    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("الإعدادات", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        // قسم الإشعارات
        item {
            Card(Modifier.fillMaxWidth()) {
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
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier.size(8.dp).clip(CircleShape)
                                        .background(
                                            if (n.read) Color.Gray else Color(0xFF6750A4)
                                        )
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(n.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
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

        // قسم قائمة الحظر
        item {
            Card(Modifier.fillMaxWidth()) {
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
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatar(name = id, size = 32.dp)
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

        // قسم عام
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Icon(Icons.Default.Tune, null,
                        tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.height(6.dp))
                    Text("عام", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(10.dp))
                    SettingRow(Icons.Default.Language, "اللغة", "العربية")
                    SettingRow(Icons.Default.Info, "الإصدار", "1.0.0-v27")
                    SettingRow(Icons.Default.Person, "إعدادات الملف الشخصي", "")
                }
            }
        }

        if (message.isNotBlank()) {
            item {
                Text(message, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String, value: String
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(20.dp), tint = Color.Gray)
        Spacer(Modifier.width(12.dp))
        Text(title, Modifier.weight(1f), fontSize = 14.sp)
        if (value.isNotBlank())
            Text(value, fontSize = 13.sp, color = Color.Gray)
    }
}

// ═══════════════════════════════════════════════════════
// مكونات مساعدة
// ═══════════════════════════════════════════════════════

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String, subtitle: String
) {
    Column(
        Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon, null,
            Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
        Spacer(Modifier.height(12.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, fontSize = 13.sp, color = Color.Gray,
            textAlign = TextAlign.Center)
    }
}

@Composable
private fun LoadingBox() {
    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun Page(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        content()
    }
}
