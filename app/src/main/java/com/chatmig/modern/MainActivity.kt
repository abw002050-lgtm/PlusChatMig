package com.example.chat.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

// ════════════════════════════════════════════════════════════════
// 1. شاشة النقاط والتحويل (Part 5)
// ════════════════════════════════════════════════════════════════

@Composable
fun PointsScreen(nav: NavHostController) {
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
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFA000))
            ) {
                Column(Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star, null, tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "رصيدك الحالي", color = Color.White, fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "$points", color = Color.White, fontSize = 44.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("نقطة", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Send, null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("تحويل النقاط", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = receiver,
                        onValueChange = { receiver = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("معرف المستلم") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it.filter(Char::isDigit) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("عدد النقاط") },
                        leadingIcon = { Icon(Icons.Default.Star, null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val n = amount.toLongOrNull()
                            if (n == null || n <= 0) message = "أدخل عددًا صحيحًا"
                            else repo.sendPoints(receiver.trim(), n) { ok, e ->
                                message = if (ok) "تم تحويل $n نقطة بنجاح ✅" else e.orEmpty()
                                if (ok) {
                                    receiver = ""
                                    amount = ""
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
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
                modifier = Modifier.fillMaxWidth(),
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(message, Modifier.padding(12.dp), fontSize = 13.sp)
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// 2. شاشات المتجر والعمليات (Part 6 & Part 7)
// ════════════════════════════════════════════════════════════════

@Composable
fun MerchantScreen(nav: NavHostController) {
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

            override fun onCancelled(e: DatabaseError) {
                error = e.message.orEmpty()
            }
        }
        repo.merchantList().addValueEventListener(l)
        onDispose { repo.merchantRef().removeEventListener(l) }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
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
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
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
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(m.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    if (m.description.isNotBlank())
                                        Text(m.description, fontSize = 12.sp, color = Color.Gray)
                                }
                                AssistChip(
                                    onClick = {},
                                    label = {
                                        Text(if (m.active) "متوفر" else "معطل", fontSize = 11.sp)
                                    },
                                    leadingIcon = {
                                        Box(
                                            Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
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
                                Icon(
                                    Icons.Default.Star, null, tint = Color(0xFFFFA000),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "${m.price} نقطة", fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFA000)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (m.ownerId != me && m.active) {
                                    Button({ selected = m }, Modifier.weight(1f)) {
                                        Icon(
                                            Icons.Default.ShoppingCart, null,
                                            Modifier.size(16.dp)
                                        )
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
                                    OutlinedButton(
                                        {
                                            repo.deleteMerchant(m.merchantId) { ok, e ->
                                                if (!ok) error = e.orEmpty()
                                            }
                                        },
                                        Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
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
        AddMerchantDialog(close = { showAdd = false }) { n, d, p ->
            repo.addMerchant(n, d, p) { ok, e ->
                showAdd = false
                if (!ok) error = e.orEmpty()
            }
        }
    }
    selected?.let { m ->
        AlertDialog(
            onDismissRequest = { selected = null },
            icon = {
                Icon(
                    Icons.Default.ShoppingCart, null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
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

@Composable
fun TransactionsScreen() {
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
                error = it.localizedMessage.orEmpty()
                loading = false
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("عمليات النقاط", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        when {
            loading -> LoadingBox()
            error.isNotBlank() -> Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(error, Modifier.padding(12.dp))
            }

            list.isEmpty() -> EmptyState(
                Icons.Default.Receipt, "لا توجد عمليات",
                "لم تقم بأي عملية بعد"
            )

            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(list) { t ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (t.type == "merchant_purchase")
                                            Color(0xFFFFA000).copy(alpha = 0.2f)
                                        else Color(0xFF6750A4).copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (t.type == "merchant_purchase")
                                        Icons.Default.ShoppingCart else Icons.Default.Send,
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
                                    Text(
                                        "المنتج: ${t.merchantName}",
                                        fontSize = 12.sp, color = Color.Gray
                                    )
                                Text(
                                    "الحالة: ${t.status}",
                                    fontSize = 11.sp, color = Color.Gray
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "${t.amount}", fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFA000), fontSize = 18.sp
                                )
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
        icon = {
            Icon(
                Icons.Default.AddShoppingCart, null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("إضافة منتج", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("اسم المنتج") },
                    leadingIcon = { Icon(Icons.Default.LocalOffer, null) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = desc, onValueChange = { desc = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("الوصف") },
                    leadingIcon = { Icon(Icons.Default.Subject, null) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = price, onValueChange = { price = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("السعر (نقاط)") },
                    leadingIcon = { Icon(Icons.Default.Star, null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = price.toLongOrNull()
                    if (name.trim().isNotEmpty() && p != null && p > 0) create(name, desc, p)
                },
                enabled = name.trim().isNotEmpty() && price.isNotBlank()
            ) { Text("حفظ") }
        },
        dismissButton = { TextButton(close) { Text("إلغاء") } }
    )
}

// ════════════════════════════════════════════════════════════════
// 3. شاشة التعليقات والإعدادات (Part 7)
// ════════════════════════════════════════════════════════════════

@Composable
fun CommentsScreen() {
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
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("كل التعليقات", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        if (list.isEmpty()) {
            EmptyState(Icons.Default.Comment, "لا توجد تعليقات", "شارك رأيك الآن!")
        } else {
            LazyColumn(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(list) { c -> CommentCard(c) }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = text, onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("اكتب تعليقًا...") },
                shape = RoundedCornerShape(24.dp), singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            IconButton({
                if (text.trim().isNotEmpty()) {
                    repo.addComment(uid, text) { ok, e ->
                        if (ok) {
                            text = ""
                            msg = "تم إرسال التعليق ✅"
                        } else msg = e.orEmpty()
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

@Composable
fun SettingsScreen() {
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
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Text("الإعدادات", fontSize = 24.sp, fontWeight = FontWeight.Bold) }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Notifications, null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "الإشعارات (${notifications.size})",
                            fontWeight = FontWeight.Bold, fontSize = 16.sp
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    if (notifications.isEmpty()) {
                        Text("لا توجد إشعارات", fontSize = 13.sp, color = Color.Gray)
                    } else {
                        notifications.take(5).forEach { n ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (n.read) Color.Gray
                                            else Color(0xFF6750A4)
                                        )
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        n.title, fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
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
                        Icon(
                            Icons.Default.Block, null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "قائمة الحظر (${blocked.size})",
                            fontWeight = FontWeight.Bold, fontSize = 16.sp
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    if (blocked.isEmpty()) {
                        Text("لا يوجد محظورون", fontSize = 13.sp, color = Color.Gray)
                    } else {
                        blocked.forEach { id ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(message, Modifier.padding(12.dp), fontSize = 13.sp)
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// 4. شاشات الأصدقاء والطلبات (Part 8, Part 9 & Part 10)
// ════════════════════════════════════════════════════════════════

@Composable
fun FriendsScreen(nav: NavHostController) {
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

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
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
                        Icon(
                            Icons.Default.Mail, null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                IconButton({ showAddDialog = true }) {
                    Icon(
                        Icons.Default.PersonAdd, null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (incoming.isNotEmpty()) {
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                onClick = { showRequestsDialog = true }
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Mail, null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "لديك ${incoming.size} طلب صداقة",
                        Modifier.weight(1f), fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Icon(
                        Icons.Default.ArrowForward, null,
                        tint = MaterialTheme.colorScheme.primary
                    )
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
        AddFriendDialog(close = { showAddDialog = false })
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
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                ChatUser(uid = f.friendId, name = f.friendName, photoUrl = f.friendPhoto),
                48.dp
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    f.friendName.ifBlank { "صديق" },
                    fontWeight = FontWeight.Bold, fontSize = 15.sp
                )
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
                            Icon(
                                Icons.Default.ChatBubble, null,
                                tint = MaterialTheme.colorScheme.primary
                            )
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
                            Icon(
                                Icons.Default.PersonRemove, null,
                                tint = MaterialTheme.colorScheme.error
                            )
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
        if (query.length < 2) {
            results = emptyList(); return@LaunchedEffect
        }
        loading = true
        kotlinx.coroutines.delay(400)
        repo.searchUsers(query) { list ->
            results = list
            loading = false
        }
    }

    AlertDialog(
        onDismissRequest = close,
        icon = {
            Icon(
                Icons.Default.PersonAdd, null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("إضافة صديق", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it; msg = "" },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("اسم المستخدم أو البريد") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (loading) {
                            CircularProgressIndicator(
                                Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp)
                )
                if (msg.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        msg, fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(8.dp))
                if (results.isEmpty() && query.length >= 2 && !loading) {
                    Text("لا توجد نتائج", fontSize = 13.sp, color = Color.Gray)
                } else {
                    LazyColumn(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(results) { u ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatar(u, 40.dp, u.online)
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        u.name.ifBlank { "مستخدم" },
                                        fontWeight = FontWeight.Bold, fontSize = 14.sp
                                    )
                                    if (u.online)
                                        Text(
                                            "متصل الآن", fontSize = 11.sp,
                                            color = Color(0xFF4CAF50)
                                        )
                                }
                                IconButton({
                                    repo.sendFriendRequest(u) { ok, e ->
                                        msg = if (ok) "✅ تم إرسال الطلب"
                                        else "خطأ: ${e ?: "غير معروف"}"
                                    }
                                }) {
                                    Icon(
                                        Icons.Default.PersonAdd, null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
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
        icon = {
            Icon(
                Icons.Default.Mail, null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
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
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(list) { req ->
                            Card(Modifier.fillMaxWidth()) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
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
                                        Text(
                                            req.fromName.ifBlank { "مستخدم" },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
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
                                            Icon(
                                                Icons.Default.Check, null,
                                                tint = Color(0xFF4CAF50)
                                            )
                                        }
                                        IconButton({
                                            repo.rejectRequest(req) {}
                                        }) {
                                            Icon(
                                                Icons.Default.Close, null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
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

// ════════════════════════════════════════════════════════════════
// 5. مكونات مساعدة للواجهة - UI Helpers (Part 11)
// ════════════════════════════════════════════════════════════════

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
private fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, null, Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.5f))
        Spacer(Modifier.height(12.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        Text(
            subtitle, fontSize = 12.sp,
            color = Color.Gray.copy(alpha = 0.8f), textAlign = TextAlign.Center
        )
    }
}

// ════════════════════════════════════════════════════════════════
// Stubs لمكونات أو نماذج افتراضية غير معرفة ضمن هذه الأجزاء
// ════════════════════════════════════════════════════════════════

@Composable
private fun UserAvatar(user: ChatUser, size: Dp, showOnline: Boolean = false) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(user.name.take(1).uppercase(), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CommentCard(comment: Comment) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(8.dp)) {
            Text(comment.text, fontSize = 13.sp)
        }
    }
}

// Stubs لكائنات الـ Repo والبيانات (تأكد من مطابقتها لما هو معرف لديك في المشروع)
class PointsRepo {
    fun observeBalance(onUpdate: (Long) -> Unit, onError: (String) -> Unit): Any = Any()
    fun removeBalanceListener(listener: Any) {}
    fun sendPoints(to: String, amount: Long, callback: (Boolean, String?) -> Unit) {}
    fun merchantList(): Any = Any()
    fun merchantRef(): Any = Any()
    fun setMerchantActive(id: String, active: Boolean, callback: (Boolean, String?) -> Unit) {}
    fun deleteMerchant(id: String, callback: (Boolean, String?) -> Unit) {}
    fun addMerchant(name: String, desc: String, price: Long, callback: (Boolean, String?) -> Unit) {}
    fun purchaseMerchant(merchant: Merchant, callback: (Boolean, String?) -> Unit) {}
    fun transactions(): Any = Any()
}

class RelationshipRepo {
    fun comments(uid: String): Any = Any()
    fun addComment(uid: String, text: String, callback: (Boolean, String?) -> Unit) {}
    fun blockList(): Any = Any()
}

class NotificationRepo {
    fun inbox(): Any = Any()
    fun markRead(id: String) {}
}

class FriendsRepo {
    fun observeFriends(onUpdate: (List<Friendship>) -> Unit): Any = Any()
    fun observeIncomingRequests(onUpdate: (List<FriendRequest>) -> Unit): Any = Any()
    fun observeOutgoingRequests(onUpdate: (List<FriendRequest>) -> Unit): Any = Any()
    fun removeFriendsListener(listener: Any) {}
    fun removeIncomingListener(listener: Any) {}
    fun removeOutgoingListener(listener: Any) {}
    fun removeFriend(friendId: String, callback: (Boolean) -> Unit) {}
    fun searchUsers(query: String, callback: (List<ChatUser>) -> Unit) {}
    fun sendFriendRequest(user: ChatUser, callback: (Boolean, String?) -> Unit) {}
    fun acceptRequest(req: FriendRequest, callback: (Boolean) -> Unit) {}
    fun rejectRequest(req: FriendRequest, callback: (Boolean) -> Unit) {}
    fun cancelOutgoing(req: FriendRequest, callback: (Boolean) -> Unit) {}
}

// Data Classes التقديرية (تُستبدل بالتي في مشروعك عند الحاجة)
data class Merchant(val merchantId: String = "", val name: String = "", val description: String = "", val price: Long = 0, val active: Boolean = true, val ownerId: String = "")
data class PointTransaction(val type: String = "", val merchantName: String = "", val status: String = "", val amount: Long = 0, val createdAt: Long = 0)
data class Comment(val text: String = "", val timestamp: Long = 0)
data class AppNotification(val id: String = "", val title: String = "", val body: String = "", val read: Boolean = false, val timestamp: Long = 0)
data class Friendship(val friendId: String = "", val friendName: String = "", val friendPhoto: String = "")
data class FriendRequest(val fromId: String = "", val toId: String = "", val fromName: String = "")
data class ChatUser(val uid: String = "", val name: String = "", val photoUrl: String = "", val online: Boolean = false)

// امتدادات وهمية لربط الـ Firebase / Any للتعليم
private fun Any.addValueEventListener(listener: ValueEventListener) {}
private fun Any.removeEventListener(listener: ValueEventListener) {}
private fun Any.limitToLast(count: Int): Any = this
private fun Any.get(): com.google.android.gms.tasks.Task<DataSnapshot> = error("Stub call")
private fun Any.child(path: String): Any = this
private fun Any.setValue(value: Any?): com.google.android.gms.tasks.Task<Void> = error("Stub call")
