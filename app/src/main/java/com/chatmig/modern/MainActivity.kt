package com.chatmig.modern

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth

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

    Scaffold(
        bottomBar = {
            NavigationBar {
                dests.forEach { d ->
                    NavigationBarItem(
                        selected = selected == d.route,
                        onClick = {
                            selected = d.route
                            nav.navigate(d.route) {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(d.icon, contentDescription = d.title) },
                        label = { Text(d.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = nav,
            startDestination = if (!openPeer.isNullOrBlank()) "chat/$openPeer" else "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { Users(nav) }
            composable("friends") { FriendsScreen(nav) }
        }
    }
}

@Composable
private fun UserAvatar(user: ChatUser, size: Dp, isOnline: Boolean = false) {
    Box(contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.name.take(1).uppercase(),
                fontSize = (size.value * 0.4).sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        if (isOnline) {
            Box(
                modifier = Modifier
                    .size(size * 0.28f)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50))
            )
        }
    }
}

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, Modifier.size(64.dp), tint = Color.Gray)
        Spacer(Modifier.height(12.dp))
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, fontSize = 14.sp, color = Color.Gray)
    }
}

@Composable
private fun LoadingBox() {
    Box(
        Modifier.fillMaxWidth().padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
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
                Modifier.fillMaxWidth().padding(bottom = 8.dp),
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
                Icons.Outlined.PeopleOutline,
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
        AddFriendDialog({ showAddDialog = false })
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
        Modifier.fillMaxWidth(),
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
                            repo.removeFriend(f.friendId) {}
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AddFriendDialog(close: () -> Unit) {
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
                                            repo.acceptRequest(req) {}
                                        }) {
                                            Icon(Icons.Default.Check, null,
                                                tint = Color(0xFF4CAF50))
                                        }
                                        IconButton({
                                            repo.rejectRequest(req) {}
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
