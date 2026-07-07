package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: VdbViewModel,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val role by viewModel.role.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val businesses by viewModel.businesses.collectAsStateWithLifecycle()
    val selectedBusiness by viewModel.selectedBusiness.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val aiLogs by viewModel.aiAdviceLogs.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val generatedPoster by viewModel.generatedPosterText.collectAsStateWithLifecycle()
    val isPosterLoading by viewModel.isPosterLoading.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    var searchInput by remember { mutableStateOf("") }

    // Navigation and structure
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(SaffronPrimary, GoldAccent)
                                    ),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "VDB Digital",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                brush = Brush.linearGradient(
                                    colors = listOf(SaffronPrimary, GoldDark)
                                )
                            )
                        )
                    }
                },
                actions = {
                    LanguageToggler(
                        currentLanguage = language,
                        onLanguageSelected = { viewModel.setLanguage(it) }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (darkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = if (darkTheme) GoldAccent else SaffronPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentScreen == "home" || currentScreen == "details",
                    onClick = { viewModel.navigateTo("home") },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text(getLocalText("Home", language), fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = currentScreen == "ai_advisor",
                    onClick = { viewModel.navigateTo("ai_advisor") },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = "AI Advisor") },
                    label = { Text("AI Advisor", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = currentScreen == "poster_generator",
                    onClick = { viewModel.navigateTo("poster_generator") },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Poster Gen") },
                    label = { Text("AI Marketing", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = currentScreen == "orders",
                    onClick = { viewModel.navigateTo("orders") },
                    icon = {
                        BadgedBox(badge = {
                            val activeCount = orders.count { it.status != "DELIVERED" }
                            if (activeCount > 0) {
                                Badge { Text(activeCount.toString()) }
                            }
                        }) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = "Orders")
                        }
                    },
                    label = { Text(getLocalText("My Orders", language), fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = currentScreen == "vendor_dashboard",
                    onClick = { viewModel.navigateTo("vendor_dashboard") },
                    icon = { Icon(Icons.Default.Storefront, contentDescription = "Vendor Hub") },
                    label = { Text(getLocalText("Vendor Hub", language), fontSize = 11.sp) }
                )
            }
        },
        modifier = modifier.testTag("main_scaffold")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    "home" -> HomeScreen(
                        viewModel = viewModel,
                        businesses = businesses,
                        language = language,
                        searchInput = searchInput,
                        onSearchChange = { searchInput = it }
                    )
                    "details" -> DetailsScreen(
                        business = selectedBusiness,
                        products = products,
                        language = language,
                        onPlaceOrder = { b, p, addr ->
                            viewModel.placeOrder(b, p, addr)
                        },
                        onBack = { viewModel.navigateTo("home") },
                        onNavigateToOrders = { viewModel.navigateTo("orders") }
                    )
                    "ai_advisor" -> AIAdvisorScreen(
                        logs = aiLogs,
                        isLoading = isAiLoading,
                        language = language,
                        onSendMessage = { viewModel.sendUserMessageToAI(it) },
                        onClearHistory = { viewModel.clearAIAdvisorHistory() }
                    )
                    "poster_generator" -> AIPosterScreen(
                        generatedPoster = generatedPoster,
                        isLoading = isPosterLoading,
                        language = language,
                        onGenerate = { name, bType, goal ->
                            viewModel.generatePosterAndPromo(name, bType, goal)
                        }
                    )
                    "orders" -> OrdersScreen(
                        orders = orders,
                        language = language
                    )
                    "vendor_dashboard" -> VendorDashboardScreen(
                        businesses = businesses,
                        language = language,
                        onAddListing = { name, desc, cat, phone, email, addr ->
                            viewModel.registerNewBusiness(name, desc, cat, phone, email, addr)
                        }
                    )
                }
            }
        }
    }
}

// ==========================================
// SCREEN 1: HOME PAGE
// ==========================================
@Composable
fun HomeScreen(
    viewModel: VdbViewModel,
    businesses: List<Business>,
    language: AppLanguage,
    searchInput: String,
    onSearchChange: (String) -> Unit
) {
    val activeCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Hero Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .drawBehind {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(SaffronPrimary, SaffronDark)
                        )
                    )
                    // Decorative Abstract Shapes representing gold digital networks
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(GoldAccent.copy(alpha = 0.25f), Color.Transparent)
                        ),
                        radius = 250.dp.toPx(),
                        center = Offset(size.width, 0f)
                    )
                }
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    SaffronBadge(text = getLocalText("Premium Local Hub", language))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Vidarbha Digital Bajaar",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        lineHeight = 34.sp
                    )
                    Text(
                        text = "आपला व्यवसाय... आपला ब्रँड... आपला अभिमान...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = GoldAccent,
                        style = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Serif)
                    )
                }

                // Apple/Stripe search bar style
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .background(Color.White, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.Gray
                        )
                        TextField(
                            value = searchInput,
                            onValueChange = { query ->
                                onSearchChange(query)
                                viewModel.setQuery(query)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_field"),
                            placeholder = {
                                Text(
                                    text = getLocalText("Search Oranges, Handlooms, Doctors...", language),
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black)
                        )
                        if (searchInput.isNotEmpty()) {
                            IconButton(onClick = {
                                onSearchChange("")
                                viewModel.setQuery("")
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }

        // Complete categories row
        val categories = listOf("All", "Agriculture", "Restaurants", "Artists", "Medical", "Real Estate", "Education")
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = getLocalText("Featured Categories", language),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isSelected = category == activeCategory
                    val cardColor = if (isSelected) SaffronPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    val icon = when (category) {
                        "All" -> Icons.Default.Apps
                        "Agriculture" -> Icons.Default.Agriculture
                        "Restaurants" -> Icons.Default.Restaurant
                        "Artists" -> Icons.Default.Palette
                        "Medical" -> Icons.Default.MedicalServices
                        "Real Estate" -> Icons.Default.HomeWork
                        else -> Icons.Default.School
                    }

                    Box(
                        modifier = Modifier
                            .background(cardColor, RoundedCornerShape(16.dp))
                            .clickable { viewModel.setCategory(category) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = icon, contentDescription = category, tint = if (isSelected) Color.White else SaffronPrimary, modifier = Modifier.size(16.dp))
                            Text(text = getCategoryLocalText(category, language), color = textColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Popular businesses section
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getLocalText("Popular Businesses", language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${businesses.size} ${getLocalText("Listed", language)}",
                    fontSize = 12.sp,
                    color = SaffronPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            if (businesses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.SearchOff, contentDescription = "No results", tint = SilverGrey, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(getLocalText("No businesses found matching query.", language), color = SilverGrey, fontSize = 14.sp)
                    }
                }
            } else {
                businesses.forEach { biz ->
                    BusinessListItem(
                        business = biz,
                        language = language,
                        onClick = { viewModel.selectBusiness(biz) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // Today's Offers Section
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = getLocalText("Today's Prime Offers", language),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    OfferBannerCard(
                        title = getLocalText("Organic Nagpur Oranges", language),
                        discount = "FLAT 15% OFF",
                        desc = "Direct from Katol farms. Clean & Sweet.",
                        bgColor = SaffronPrimary
                    )
                }
                item {
                    OfferBannerCard(
                        title = getLocalText("Pure Bhandara Silk", language),
                        discount = "FREE DELIVERY",
                        desc = "Authentic premium silk sarees with certificate.",
                        bgColor = GoldDark
                    )
                }
                item {
                    OfferBannerCard(
                        title = getLocalText("Tadoba Jungle Honey", language),
                        discount = "BUY 1 GET 1 FREE",
                        desc = "100% pure organic herbal nectar.",
                        bgColor = VelvetDark
                    )
                }
            }
        }

        // Business Growth Statistics
        GlassyCard(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = getLocalText("VDB Digital Impact", language),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatColumn(number = "500+", label = getLocalText("Farmers Joined", language))
                StatColumn(number = "1,200+", label = getLocalText("Local Brands", language))
                StatColumn(number = "98%", label = getLocalText("Delivery Success", language))
            }
        }

        // Success Stories Section
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = getLocalText("Community Success Stories", language),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            SuccessStoryCard(
                name = "Kishor Patil (Farmer, Katol)",
                story = getLocalText("patil_story", language)
            )
            Spacer(modifier = Modifier.height(8.dp))
            SuccessStoryCard(
                name = "Sulochana Tai (Handloom Weaver, Bhandara)",
                story = getLocalText("tai_story", language)
            )
        }

        // Membership plans
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = getLocalText("Grow Your Brand - Premium Membership", language),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            MembershipCard(
                planName = "VDB Saffron Plan",
                price = "₹999 / year",
                benefits = listOf("Verified Golden Badge", "Top 3 Search Placement", "AI Poster & Reels Assistant (Unlimited)", "WhatsApp Store Link Setup"),
                highlightColor = SaffronPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            MembershipCard(
                planName = "VDB Gold Elite",
                price = "₹2,499 / year",
                benefits = listOf("Dedicated AI Account Manager", "Live GPS Delivery Fleet Access", "GST & Custom Invoice Dashboard", "Sponsor Ads Manager Slots"),
                highlightColor = GoldDark
            )
        }

        // FAQ Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp)
        ) {
            Text(
                text = "FAQ & Support",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            FAQItem(q = "What is Vidarbha Digital Bajaar?", a = "VDB is a super platform empowering local businesses, startups, creators, and farmers from Vidarbha to build, promote, and sell their products digitally.")
            FAQItem(q = "How does AI Advisor help my business?", a = "The AI advisor is powered by Gemini. You can ask business growth strategies, social media ideas, and SEO guidelines directly in English, Marathi, or Hindi.")
            FAQItem(q = "Is delivery support automated?", a = "Yes, VDB simulates real-time delivery logistics. When customers purchase from restaurants, grocers, or pharmacies, VDB triggers immediate fleet assignment and live status reports.")
        }
    }
}

// ==========================================
// SCREEN 2: DETAILS PAGE & LOCAL ORDERING
// ==========================================
@Composable
fun DetailsScreen(
    business: Business?,
    products: List<Product>,
    language: AppLanguage,
    onPlaceOrder: (Business, Product, String) -> Unit,
    onBack: () -> Unit,
    onNavigateToOrders: () -> Unit
) {
    if (business == null) return
    var showCheckoutDialog by remember { mutableStateOf<Product?>(null) }
    var deliveryAddressInput by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Banner Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .drawBehind {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(SaffronPrimary.copy(alpha = 0.85f), VelvetDark)
                        )
                    )
                }
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        SaffronBadge(text = business.category)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = business.name,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (business.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    tint = GoldAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Contact details and description
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = when (language) {
                    AppLanguage.MARATHI -> business.descriptionMr.ifBlank { business.description }
                    AppLanguage.HINDI -> business.descriptionHi.ifBlank { business.description }
                    AppLanguage.ENGLISH -> business.description
                },
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Contact details buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ContactPill(icon = Icons.Default.Phone, label = business.phone, modifier = Modifier.weight(1f))
                ContactPill(icon = Icons.Default.Email, label = business.email, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(6.dp))
            ContactPill(icon = Icons.Default.LocationOn, label = business.address, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(24.dp))

            // Products and Listings list
            Text(
                text = getLocalText("Available Products & Services", language),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(getLocalText("No products listed yet.", language), color = SilverGrey)
                }
            } else {
                products.forEach { prod ->
                    ProductCardItem(
                        product = prod,
                        language = language,
                        onBuy = { showCheckoutDialog = prod }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }

    // Modern Checkout Dialog
    if (showCheckoutDialog != null) {
        val prod = showCheckoutDialog!!
        AlertDialog(
            onDismissRequest = { showCheckoutDialog = null },
            title = { Text(getLocalText("Confirm Local Order", language), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "${getLocalText("Item", language)}: ${prod.name}", fontWeight = FontWeight.SemiBold)
                    Text(text = "${getLocalText("Price", language)}: ₹${prod.price}", color = SaffronPrimary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = getLocalText("Enter Delivery Address:", language), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = deliveryAddressInput,
                        onValueChange = { deliveryAddressInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("address_input"),
                        placeholder = { Text("E.g. Shankar Nagar, Nagpur") },
                        singleLine = true
                    )
                    Text(
                        text = "VDB Escrow Enabled: Payment is securely held until proof of delivery is confirmed.",
                        fontSize = 10.sp,
                        color = GoldDark,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (deliveryAddressInput.isNotBlank()) {
                            onPlaceOrder(business, prod, deliveryAddressInput)
                            showCheckoutDialog = null
                            deliveryAddressInput = ""
                            onNavigateToOrders()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text(getLocalText("Place Order", language))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheckoutDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ==========================================
// SCREEN 3: AI BUSINESS GROWTH ADVISOR
// ==========================================
@Composable
fun AIAdvisorScreen(
    logs: List<AIAdvice>,
    isLoading: Boolean,
    language: AppLanguage,
    onSendMessage: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    var chatInput by remember { mutableStateOf("") }
    val listState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Chat Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AI Business Advisor",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SaffronPrimary
                )
                Text(
                    text = getLocalText("Power scaling for local brands", language),
                    fontSize = 12.sp,
                    color = SilverGrey
                )
            }
            IconButton(onClick = onClearHistory) {
                Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear Chat", tint = SaffronPrimary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chat Log Scroll
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(listState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                logs.forEach { log ->
                    val isUser = log.sender == "USER"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .background(
                                    color = if (isUser) SaffronPrimary else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isUser) 16.dp else 4.dp,
                                        bottomEnd = if (isUser) 4.dp else 16.dp
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isUser) Color.Transparent else GoldAccent.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = log.messageText,
                                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                if (isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = SaffronPrimary)
                                Text("AI Growth Advisor is thinking...", fontSize = 12.sp, color = SaffronPrimary)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Suggested Prompt Chips
        val suggestions = when (language) {
            AppLanguage.MARATHI -> listOf("माझा संत्रा व्यवसाय कसा वाढवू?", "मार्केटिंग घोषणा सांगा", "ऑनलाईन विक्री कशी करावी?")
            AppLanguage.HINDI -> listOf("संत्री का बिजनेस कैसे बढ़ाएं?", "मार्केटिंग स्लोगन बताएं", "ऑनलाइन बिक्री कैसे करें?")
            AppLanguage.ENGLISH -> listOf("How to scale Katol orange farms?", "Marketing slogans for sarees", "SEO optimization tips")
        }
        
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(suggestions) { promptText ->
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .border(1.dp, SaffronPrimary.copy(alpha = 0.3f), CircleShape)
                        .clickable { onSendMessage(promptText) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = promptText, fontSize = 11.sp, color = SaffronPrimary, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Typing Field
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = chatInput,
                onValueChange = { chatInput = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_field"),
                placeholder = { Text(getLocalText("Ask growth strategies...", language)) },
                shape = RoundedCornerShape(12.dp),
                maxLines = 3,
                singleLine = false
            )
            IconButton(
                onClick = {
                    if (chatInput.isNotBlank()) {
                        onSendMessage(chatInput)
                        chatInput = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(SaffronPrimary, CircleShape)
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

// ==========================================
// SCREEN 4: AI MARKETING & POSTER MAKER
// ==========================================
@Composable
fun AIPosterScreen(
    generatedPoster: Map<String, String>?,
    isLoading: Boolean,
    language: AppLanguage,
    onGenerate: (String, String, String) -> Unit
) {
    var bizName by remember { mutableStateOf("") }
    var bizType by remember { mutableStateOf("") }
    var promoGoal by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "AI Marketing Studio",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = SaffronPrimary
        )
        Text(
            text = getLocalText("Generate instant social posters & reel scripts", language),
            fontSize = 12.sp,
            color = SilverGrey,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        GlassyCard(modifier = Modifier.fillMaxWidth()) {
            Text("Generate Brand Copywriter Kit", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SaffronPrimary)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = bizName,
                onValueChange = { bizName = it },
                label = { Text("Business Name") },
                placeholder = { Text("E.g. Katol Orange Harvest") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("poster_biz_name"),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = bizType,
                onValueChange = { bizType = it },
                label = { Text("Business Type / Category") },
                placeholder = { Text("E.g. Organic Fruit Farmer") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("poster_biz_type"),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = promoGoal,
                onValueChange = { promoGoal = it },
                label = { Text("Promotion Goal / Discount") },
                placeholder = { Text("E.g. Celebrate monsoon with 10% off") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("poster_promo_goal"),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (bizName.isNotBlank() && bizType.isNotBlank()) {
                        onGenerate(bizName, bizType, promoGoal)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("poster_generate_button"),
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI is writing brand kit...")
                } else {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Magic")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Generate Marketing Kit")
                }
            }
        }

        if (generatedPoster != null) {
            Spacer(modifier = Modifier.height(20.dp))

            // RENDER LATEST SOCIAL POSTER MOCKUP CANVAS (HIGH VISUAL CRAFT!)
            Text(
                text = "Live Social Poster Mockup",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(RichBlack, VelvetDark)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(2.dp, Brush.linearGradient(colors = listOf(SaffronPrimary, GoldAccent)), RoundedCornerShape(16.dp))
                    .drawBehind {
                        // Custom vector overlays representing gold digital networks
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(GoldAccent.copy(alpha = 0.15f), Color.Transparent)
                            ),
                            radius = 180.dp.toPx(),
                            center = Offset(0f, 0f)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(SaffronPrimary.copy(alpha = 0.1f), Color.Transparent)
                            ),
                            radius = 200.dp.toPx(),
                            center = Offset(size.width, size.height)
                        )
                    }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = bizName.uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldAccent,
                            letterSpacing = 1.sp
                        )
                        SaffronBadge(text = "Verified Brand")
                    }

                    // Slogan display (The hero text)
                    Text(
                        text = "\"${generatedPoster["SLOGAN"]}\"",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 26.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    // Footer
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Promoted via VDB Digital SuperApp",
                            fontSize = 10.sp,
                            color = SilverGrey,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.TrendingUp, contentDescription = "VDB", tint = SaffronPrimary, modifier = Modifier.size(12.dp))
                            Text(
                                text = "आपला व्यवसाय... आपला ब्रँड... आपला अभिमान...",
                                fontSize = 8.sp,
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // COPYWRITER KIT BLOCKS
            KitTextBlock(title = "✨ Suggested Brand Slogan", text = generatedPoster["SLOGAN"] ?: "")
            KitTextBlock(title = "📝 Social Media Caption", text = generatedPoster["CAPTION"] ?: "")
            KitTextBlock(title = "🎬 15-Sec Reel Script Idea", text = generatedPoster["REEL_SCRIPT"] ?: "")
            KitTextBlock(title = "🔑 SEO Keywords", text = generatedPoster["KEYWORDS"] ?: "")
        }
    }
}

// ==========================================
// SCREEN 5: CUSTOMER DASHBOARD & ORDERS
// ==========================================
@Composable
fun OrdersScreen(
    orders: List<Order>,
    language: AppLanguage
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = getLocalText("My Orders & Delivery Tracker", language),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = SaffronPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Customer loyalty point summary card
        GlassyCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            borderColor = GoldAccent.copy(alpha = 0.3f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("VDB Local Loyalty Points", fontSize = 12.sp, color = SilverGrey)
                    Text("240 Points", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                }
                Icon(imageVector = Icons.Default.CardMembership, contentDescription = "Points", tint = GoldAccent, modifier = Modifier.size(36.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Earn points on local deliveries. Redeem ₹10 per 10 points at any verified VDB merchant store.", fontSize = 10.sp, color = PlatinumWhite)
        }

        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = "No orders", tint = SilverGrey, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(getLocalText("You have no active orders.", language), color = SilverGrey)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders) { ord ->
                    OrderTrackerCard(order = ord, language = language)
                }
            }
        }
    }
}

// ==========================================
// SCREEN 6: VENDOR DASHBOARD PANEL
// ==========================================
@Composable
fun VendorDashboardScreen(
    businesses: List<Business>,
    language: AppLanguage,
    onAddListing: (String, String, String, String, String, String) -> Unit
) {
    var showAddListingDialog by remember { mutableStateOf(false) }
    var bizName by remember { mutableStateOf("") }
    var bizDesc by remember { mutableStateOf("") }
    var bizCat by remember { mutableStateOf("Restaurants") }
    var bizPhone by remember { mutableStateOf("") }
    var bizEmail by remember { mutableStateOf("") }
    var bizAddress by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "VDB Vendor Panel",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SaffronPrimary
                )
                Text(
                    text = "Manage your listings and live storefront",
                    fontSize = 12.sp,
                    color = SilverGrey
                )
            }
            PremiumButton(
                text = "+ Add Shop",
                onClick = { showAddListingDialog = true },
                testTag = "add_shop_fab",
                modifier = Modifier.height(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick vendor analytic widgets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Total Views", fontSize = 11.sp, color = SilverGrey)
                    Text("14,820", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SaffronPrimary)
                }
            }
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Escrow Ledger", fontSize = 11.sp, color = SilverGrey)
                    Text("₹4,250", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Your Live Storefronts",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val vendorShops = businesses.filter { it.ownerEmail == "vendor@vdb.com" }
        if (vendorShops.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No live storefronts registered yet. Click '+ Add Shop' above to start selling!", textAlign = TextAlign.Center, color = SilverGrey, fontSize = 13.sp)
            }
        } else {
            vendorShops.forEach { shop ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(shop.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(imageVector = Icons.Default.Verified, contentDescription = "Verified", tint = GoldAccent, modifier = Modifier.size(16.dp))
                            }
                            Text(shop.category, fontSize = 12.sp, color = SaffronPrimary)
                        }
                        IconButton(onClick = { /* Manage products */ }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = SilverGrey)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Digital QR Visiting Card Simulator (Premium details!)
        Text(
            text = "Your Business Digital Card",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        GlassyCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Mock QR
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.QrCode2, contentDescription = "QR Code", tint = Color.Black, modifier = Modifier.fillMaxSize())
                }

                Column {
                    Text("Vidarbha Digital QR visiting card", fontSize = 11.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                    Text("Instant QR Storefront", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("Place on your shop banner or brochure. Customers scan this QR to instantly view your products on the VDB Digital SuperApp!", fontSize = 11.sp, color = PlatinumWhite, lineHeight = 15.sp)
                }
            }
        }
    }

    // Add Listing Dialog Modal
    if (showAddListingDialog) {
        val categories = listOf("Restaurants", "Agriculture", "Artists", "Medical", "Real Estate", "Education")
        AlertDialog(
            onDismissRequest = { showAddListingDialog = false },
            title = { Text("Register Your Business Shop", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(value = bizName, onValueChange = { bizName = it }, label = { Text("Shop / Brand Name") }, modifier = Modifier.fillMaxWidth().testTag("add_shop_name"))
                    OutlinedTextField(value = bizDesc, onValueChange = { bizDesc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth().testTag("add_shop_desc"))
                    
                    // Category dropdown / selection
                    Text("Select Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = cat == bizCat
                            Box(
                                modifier = Modifier
                                    .background(if (isSelected) SaffronPrimary else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                    .clickable { bizCat = cat }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(cat, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    OutlinedTextField(value = bizPhone, onValueChange = { bizPhone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth().testTag("add_shop_phone"))
                    OutlinedTextField(value = bizEmail, onValueChange = { bizEmail = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth().testTag("add_shop_email"))
                    OutlinedTextField(value = bizAddress, onValueChange = { bizAddress = it }, label = { Text("Complete Shop Address") }, modifier = Modifier.fillMaxWidth().testTag("add_shop_address"))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (bizName.isNotBlank() && bizDesc.isNotBlank() && bizPhone.isNotBlank() && bizAddress.isNotBlank()) {
                            onAddListing(bizName, bizDesc, bizCat, bizPhone, bizEmail, bizAddress)
                            showAddListingDialog = false
                            // Clear inputs
                            bizName = ""
                            bizDesc = ""
                            bizPhone = ""
                            bizEmail = ""
                            bizAddress = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text("Register & Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddListingDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ==========================================
// SUB-COMPONENTS & LAYOUT PARTS
// ==========================================
@Composable
fun BusinessListItem(
    business: Business,
    language: AppLanguage,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = business.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (business.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = GoldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                SaffronBadge(text = getCategoryLocalText(business.category, language))
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = when (language) {
                    AppLanguage.MARATHI -> business.descriptionMr.ifBlank { business.description }
                    AppLanguage.HINDI -> business.descriptionHi.ifBlank { business.description }
                    AppLanguage.ENGLISH -> business.description
                },
                fontSize = 13.sp,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StarRatingBar(rating = business.rating, reviewCount = business.reviewCount)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Location", tint = SaffronPrimary, modifier = Modifier.size(14.dp))
                    Text(text = business.address.split(",").last().trim(), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
fun ProductCardItem(
    product: Product,
    language: AppLanguage,
    onBuy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (language == AppLanguage.MARATHI && product.nameMr.isNotBlank()) product.nameMr else product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = product.description,
                    fontSize = 11.sp,
                    color = SilverGrey,
                    maxLines = 2,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = "₹${product.price}",
                    color = SaffronPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            PremiumButton(
                text = getLocalText("Buy", language),
                onClick = onBuy,
                modifier = Modifier.height(36.dp)
            )
        }
    }
}

@Composable
fun OrderTrackerCard(
    order: Order,
    language: AppLanguage
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(order.businessName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(order.productName, fontSize = 13.sp, color = SaffronPrimary)
                }
                Text("₹${order.price}", fontWeight = FontWeight.Bold, color = SaffronPrimary)
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)

            // Step Progress tracker representing actual route state
            val steps = listOf("PENDING", "ASSIGNED", "OUT_FOR_DELIVERY", "DELIVERED")
            val activeIndex = steps.indexOf(order.status)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, step ->
                    val isCompleted = index <= activeIndex
                    val stepColor = if (isCompleted) SaffronPrimary else SilverGrey.copy(alpha = 0.4f)
                    val stepIcon = when (step) {
                        "PENDING" -> Icons.Default.ReceiptLong
                        "ASSIGNED" -> Icons.Default.LocalShipping
                        "OUT_FOR_DELIVERY" -> Icons.Default.Navigation
                        else -> Icons.Default.CheckCircle
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(stepColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = stepIcon, contentDescription = step, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = getStatusLocalText(step, language),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = stepColor,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (index < steps.size - 1) {
                        Box(
                            modifier = Modifier
                                .height(2.dp)
                                .weight(0.5f)
                                .background(if (index < activeIndex) SaffronPrimary else SilverGrey.copy(alpha = 0.2f))
                        )
                    }
                }
            }

            if (order.status == "OUT_FOR_DELIVERY") {
                Spacer(modifier = Modifier.height(12.dp))
                // Render custom vector map GPS track mockup
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = SaffronPrimary)
                        Text("Live GPS Optimization Tracker: Partner is 0.5 km away from your address...", fontSize = 10.sp, color = SaffronPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ContactPill(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = SaffronPrimary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
fun OfferBannerCard(
    title: String,
    discount: String,
    desc: String,
    bgColor: Color
) {
    Box(
        modifier = Modifier
            .width(260.dp)
            .height(130.dp)
            .background(bgColor, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(text = discount, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Column {
                Text(text = title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(text = desc, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun StatColumn(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = number, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = GoldAccent)
        Text(text = label, fontSize = 11.sp, color = PlatinumWhite.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SuccessStoryCard(name: String, story: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(imageVector = Icons.Default.ChatBubble, contentDescription = "Story", tint = SaffronPrimary, modifier = Modifier.size(16.dp))
                Text(text = name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = story, fontSize = 12.sp, color = SilverGrey, lineHeight = 16.sp)
        }
    }
}

@Composable
fun MembershipCard(
    planName: String,
    price: String,
    benefits: List<String>,
    highlightColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, highlightColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = planName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = highlightColor)
                Text(text = price, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = highlightColor)
            }
            Spacer(modifier = Modifier.height(10.dp))
            benefits.forEach { benefit ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Checked", tint = highlightColor, modifier = Modifier.size(14.dp))
                    Text(text = benefit, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { /* sponsorship action */ },
                colors = ButtonDefaults.buttonColors(containerColor = highlightColor),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Select & Subscribe", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FAQItem(q: String, a: String) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = q, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = "Expand",
                tint = SaffronPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
        if (expanded) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = a, fontSize = 12.sp, color = SilverGrey, lineHeight = 16.sp)
        }
    }
}

@Composable
fun KitTextBlock(title: String, text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SaffronPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = text, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp)
        }
    }
}

// ==========================================
// STRING LOCALIZER HELPERS
// ==========================================
fun getLocalText(key: String, lang: AppLanguage): String {
    return when (lang) {
        AppLanguage.MARATHI -> when (key) {
            "Home" -> "मुख्यपृष्ठ"
            "My Orders" -> "माझ्या ऑर्डर्स"
            "Vendor Hub" -> "विक्रेता केंद्र"
            "Search Oranges, Handlooms, Doctors..." -> "संत्री, हातमाग, डॉक्टर शोधा..."
            "Featured Categories" -> "मुख्य श्रेणी"
            "Popular Businesses" -> "लोकप्रिय व्यवसाय"
            "Listed" -> "नोंदणीकृत"
            "Today's Prime Offers" -> "आजच्या मुख्य ऑफर्स"
            "Buy" -> "खरेदी करा"
            "Item" -> "उत्पादन"
            "Price" -> "किंमत"
            "Place Order" -> "ऑर्डर द्या"
            "Confirm Local Order" -> "ऑर्डर निश्चित करा"
            "Enter Delivery Address:" -> "डिलिव्हरी पत्ता टाका:"
            "Ask growth strategies..." -> "व्यवसाय वाढवण्याच्या टिप्स विचारा..."
            "Generate instant social posters & reel scripts" -> "सोशल पोस्टर्स आणि रील स्क्रिप्ट तयार करा"
            "Power scaling for local brands" -> "स्थानिक ब्रँड्सच्या वाढीसाठी विशेष मार्गदर्शन"
            "My Orders & Delivery Tracker" -> "माझ्या ऑर्डर्स आणि ट्रॅकर"
            "You have no active orders." -> "तुमची कोणतीही सक्रिय ऑर्डर नाही."
            "No businesses found matching query." -> "या नावाने कोणताही व्यवसाय सापडला नाही."
            "Available Products & Services" -> "उपलब्ध उत्पादने आणि सेवा"
            "No products listed yet." -> "अद्याप कोणतीही उत्पादने सूचीबद्ध केलेली नाहीत."
            "Organic Nagpur Oranges" -> "सेंद्रिय नागपुरी संत्री"
            "Pure Bhandara Silk" -> "शुद्ध भंडारा सिल्क"
            "Tadoba Jungle Honey" -> "ताडोबा जंगल मध"
            "VDB Digital Impact" -> "विदर्भ डिजिटल प्रभाव"
            "Farmers Joined" -> "शेतकरी जोडले"
            "Local Brands" -> "स्थानिक ब्रँड"
            "Delivery Success" -> "यशस्वी डिलिव्हरी"
            "Community Success Stories" -> "यशस्वी यशोगाथा"
            "Grow Your Brand - Premium Membership" -> "आपला ब्रँड वाढवा - प्रीमियम सदस्यता"
            "patil_story" -> "विदर्भ डिजिटल बाजारमुळे मी माझ्या काटोलच्या शेतातील ५ टन संत्री मध्यस्थींशिवाय थेट ग्राहकांना विकली. ५०% जास्त नफा मिळाला!"
            "tai_story" -> "मी माझ्या भंडारा येथील घरी तयार केलेल्या कोसा सिल्क साड्या व्हीडीबीवर लिस्ट केल्या. आज मला संपूर्ण महाराष्ट्रातून ऑर्डर्स मिळत आहेत."
            else -> key
        }
        AppLanguage.HINDI -> when (key) {
            "Home" -> "मुख्यपृष्ठ"
            "My Orders" -> "मेरी ऑर्डर्स"
            "Vendor Hub" -> "विक्रेता केंद्र"
            "Search Oranges, Handlooms, Doctors..." -> "संतरे, हथकरघा, डॉक्टर खोजें..."
            "Featured Categories" -> "मुख्य श्रेणियां"
            "Popular Businesses" -> "लोकप्रिय व्यवसाय"
            "Listed" -> "पंजीकृत"
            "Today's Prime Offers" -> "आज के मुख्य ऑफर्स"
            "Buy" -> "खरीदें"
            "Item" -> "उत्पाद"
            "Price" -> "कीमत"
            "Place Order" -> "ऑर्डर दें"
            "Confirm Local Order" -> "ऑर्डर कन्फर्म करें"
            "Enter Delivery Address:" -> "डिलिवरी का पता दर्ज करें:"
            "Ask growth strategies..." -> "व्यवसाय बढ़ाने के उपाय पूछें..."
            "Generate instant social posters & reel scripts" -> "सोशल पोस्टर्स और रील स्क्रिप्ट बनाएं"
            "Power scaling for local brands" -> "स्थानीय ब्रांड्स के विकास के लिए विशेष सलाह"
            "My Orders & Delivery Tracker" -> "मेरी ऑर्डर्स और ट्रैकर"
            "You have no active orders." -> "आपकी कोई सक्रिय ऑर्डर नहीं है."
            "No businesses found matching query." -> "इस नाम से कोई व्यवसाय नहीं मिला."
            "Available Products & Services" -> "उपलब्ध उत्पाद और सेवाएं"
            "No products listed yet." -> "अभी तक कोई उत्पाद सूचीबद्ध नहीं हैं."
            "Organic Nagpur Oranges" -> "जैविक नागपुरी संतरे"
            "Pure Bhandara Silk" -> "शुद्ध भंडारा सिल्क"
            "Tadoba Jungle Honey" -> "ताडोबा जंगल शहद"
            "VDB Digital Impact" -> "विदर्भ डिजिटल प्रभाव"
            "Farmers Joined" -> "किसान जुड़े"
            "Local Brands" -> "स्थानीय ब्रांड"
            "Delivery Success" -> "सफल डिलीवरी"
            "Community Success Stories" -> "सफल कहानियां"
            "Grow Your Brand - Premium Membership" -> "अपना ब्रांड बढ़ाएं - प्रीमियम सदस्यता"
            "patil_story" -> "विदर्भ डिजिटल बाजार की मदद से मैंने काटोल के अपने खेत से ५ टन संतरे सीधे ग्राहकों को बेचे। मुझे ५०% अधिक मुनाफा हुआ!"
            "tai_story" -> "मैंने भंडारा में अपने घर पर तैयार की गई कोसा सिल्क साड़ियों को सूचीबद्ध किया। आज मुझे पूरे महाराष्ट्र से ऑर्डर्स मिल रहे हैं।"
            else -> key
        }
        AppLanguage.ENGLISH -> when (key) {
            "patil_story" -> "With Vidarbha Digital Bajaar, I bypassed wholesalers and sold 5 tons of my premium Katol oranges directly to Nagpur customers, earning 50% higher profits."
            "tai_story" -> "Listing our generational Bhandara Kosa silk handlooms on VDB transformed our lives. We now ship authentic handmade sarees across Maharashtra."
            else -> key
        }
    }
}

fun getCategoryLocalText(cat: String, lang: AppLanguage): String {
    return when (lang) {
        AppLanguage.MARATHI -> when (cat) {
            "All" -> "सर्व"
            "Agriculture" -> "कृषी व शेती"
            "Restaurants" -> "हॉटेल्स व फूड"
            "Artists" -> "कलाकार व हातमाग"
            "Medical" -> "वैद्यकीय व औषध"
            "Real Estate" -> "रिअल इस्टेट"
            "Education" -> "शिक्षण"
            else -> cat
        }
        AppLanguage.HINDI -> when (cat) {
            "All" -> "सभी"
            "Agriculture" -> "कृषि और खेती"
            "Restaurants" -> "होटल और फूड"
            "Artists" -> "कलाकार और हथकरघा"
            "Medical" -> "चिकित्सा और दवा"
            "Real Estate" -> "रियल एस्टेट"
            "Education" -> "शिक्षा"
            else -> cat
        }
        AppLanguage.ENGLISH -> cat
    }
}

fun getStatusLocalText(status: String, lang: AppLanguage): String {
    return when (lang) {
        AppLanguage.MARATHI -> when (status) {
            "PENDING" -> "प्रलंबित"
            "ASSIGNED" -> "वाहन नियुक्त"
            "OUT_FOR_DELIVERY" -> "डिलिव्हरीसाठी मार्गस्थ"
            "DELIVERED" -> "वितरित झाले"
            else -> status
        }
        AppLanguage.HINDI -> when (status) {
            "PENDING" -> "लंबित"
            "ASSIGNED" -> "वाहन नियुक्त"
            "OUT_FOR_DELIVERY" -> "डिलिवरी के लिए रवाना"
            "DELIVERED" -> "वितरित हुआ"
            else -> status
        }
        AppLanguage.ENGLISH -> status
    }
}
