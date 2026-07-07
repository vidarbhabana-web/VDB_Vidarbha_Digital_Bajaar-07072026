package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class VdbViewModel(application: Application) : AndroidViewModel(application) {
    private val db = VdbDatabase.getDatabase(application)
    private val repository = VdbRepository(db)

    // Language State
    private val _language = MutableStateFlow(AppLanguage.ENGLISH)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    // Role State
    private val _role = MutableStateFlow(UserRole.CUSTOMER)
    val role: StateFlow<UserRole> = _role.asStateFlow()

    // Screen navigation state
    private val _currentScreen = MutableStateFlow("home") // home, details, ai_advisor, poster_generator, vendor_dashboard, orders, DigitalCard
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Selected business for details
    private val _selectedBusiness = MutableStateFlow<Business?>(null)
    val selectedBusiness: StateFlow<Business?> = _selectedBusiness.asStateFlow()

    // Search & Category filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Active Businesses list
    val businesses: StateFlow<List<Business>> = combine(
        repository.allBusinesses,
        _searchQuery,
        _selectedCategory
    ) { all, query, category ->
        var list = all
        if (query.isNotBlank()) {
            list = list.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true) ||
                it.descriptionMr.contains(query, ignoreCase = true) ||
                it.descriptionHi.contains(query, ignoreCase = true)
            }
        }
        if (category != "All") {
            list = list.filter { it.category == category }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Products list for selected business
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    // Active orders list
    val orders: StateFlow<List<Order>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Chat Advisor State
    val aiAdviceLogs: StateFlow<List<AIAdvice>> = repository.aiLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // AI Poster & Caption Generator State
    private val _generatedPosterText = MutableStateFlow<Map<String, String>?>(null)
    val generatedPosterText: StateFlow<Map<String, String>?> = _generatedPosterText.asStateFlow()

    private val _isPosterLoading = MutableStateFlow(false)
    val isPosterLoading: StateFlow<Boolean> = _isPosterLoading.asStateFlow()

    init {
        seedInitialData()
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
    }

    fun setRole(role: UserRole) {
        _role.value = role
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun selectBusiness(business: Business) {
        _selectedBusiness.value = business
        if (business != null) {
            viewModelScope.launch {
                repository.getProductsByBusiness(business.id).collect {
                    _products.value = it
                }
            }
        }
        navigateTo("details")
    }

    fun setQuery(q: String) {
        _searchQuery.value = q
    }

    fun setCategory(cat: String) {
        _selectedCategory.value = cat
    }

    // AI Advisor interaction
    fun sendUserMessageToAI(message: String) {
        if (message.isBlank()) return
        viewModelScope.launch {
            val userLog = AIAdvice(sender = "USER", messageText = message)
            repository.insertAdvice(userLog)

            _isAiLoading.value = true

            // Generate system instructions based on the language
            val systemInstr = when (_language.value) {
                AppLanguage.MARATHI -> "तुम्ही 'विदर्भ डिजिटल बाजार'चे अधिकृत एआय बिझनेस ग्रोथ ॲडव्हायझर आहात. विदर्भातील स्थानिक उद्योजक, शेतकरी आणि छोट्या व्यावसायिकांना त्यांचा व्यवसाय वाढवण्यासाठी अत्यंत मौल्यवान आणि सोप्या मराठी भाषेत मार्गदर्शन करा."
                AppLanguage.HINDI -> "आप 'विदर्भ डिजिटल बाजार' के आधिकारिक एआई बिजनेस ग्रोथ एडवाइजर हैं। विदर्भ के स्थानीय उद्यमियों, किसानों और छोटे व्यवसायियों को उनका व्यवसाय बढ़ाने के लिए हिंदी भाषा में मूल्यवान मार्गदर्शन प्रदान करें।"
                AppLanguage.ENGLISH -> "You are the official AI Business Growth Advisor for Vidarbha Digital Bajaar (VDB). Guide local entrepreneurs, farmers, and micro-businesses of Vidarbha region to grow their business digitally. Keep your advice tailored to Vidarbha, highly actionable, friendly, and in English."
            }

            val response = GeminiService.generateContent(message, systemInstruction = systemInstr)
            val aiLog = AIAdvice(sender = "AI", messageText = response)
            repository.insertAdvice(aiLog)
            _isAiLoading.value = false
        }
    }

    fun clearAIAdvisorHistory() {
        viewModelScope.launch {
            repository.clearAdviceLogs()
            // Add custom welcome greeting in selected language
            val welcomeText = when (_language.value) {
                AppLanguage.MARATHI -> "नमस्कार! मी आपला विदर्भ डिजिटल बाजार एआय सल्लागार आहे. आपल्या व्यवसायाच्या वाढीसाठी मी आपल्याला कशी मदत करू?"
                AppLanguage.HINDI -> "नमस्कार! मैं आपका विदर्भ डिजिटल बाजार एआई सलाहकार हूँ। अपने व्यवसाय के विकास के लिए मैं आपकी क्या सहायता कर सकता हूँ?"
                AppLanguage.ENGLISH -> "Namaskar! I am your Vidarbha Digital Bajaar AI Growth Advisor. How can I assist you in scaling your business digitally today?"
            }
            repository.insertAdvice(AIAdvice(sender = "AI", messageText = welcomeText))
        }
    }

    // AI Poster & Caption Generator
    fun generatePosterAndPromo(businessName: String, businessType: String, promoGoal: String) {
        viewModelScope.launch {
            _isPosterLoading.value = true
            _generatedPosterText.value = null

            val langStr = when (_language.value) {
                AppLanguage.MARATHI -> "मराठी (Marathi)"
                AppLanguage.HINDI -> "हिंदी (Hindi)"
                AppLanguage.ENGLISH -> "English"
            }

            val prompt = """
                Generate a professional marketing kit for a local business in Vidarbha, Maharashtra.
                Business Name: $businessName
                Business Category: $businessType
                Promotion Goal: $promoGoal
                Preferred Language: $langStr

                Return the output as plain text in the following labeled sections exactly (do not output JSON):
                [SLOGAN] - (A catchy, inspiring brand slogan incorporating the business essence)
                [CAPTION] - (A persuasive, highly engaging social media caption with local flavor and emojis)
                [REEL_SCRIPT] - (A short 15-second reel outline/script including camera visual ideas and spoken words)
                [KEYWORDS] - (5 relevant SEO/social tags)
            """.trimIndent()

            val systemInstr = "You are a professional brand strategist and copywriter specializing in boosting local businesses across Maharashtra. Write high-converting marketing text."
            val rawOutput = GeminiService.generateContent(prompt, systemInstruction = systemInstr)

            // Parse response fields manually for UI robustness
            val map = parseKitResponse(rawOutput)
            _generatedPosterText.value = map
            _isPosterLoading.value = false
        }
    }

    private fun parseKitResponse(raw: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val sections = listOf("SLOGAN", "CAPTION", "REEL_SCRIPT", "KEYWORDS")
        
        var currentSection = ""
        val currentContent = StringBuilder()

        raw.lines().forEach { line ->
            var matched = false
            for (sec in sections) {
                if (line.trim().startsWith("[$sec]", ignoreCase = true) || line.trim().startsWith("$sec:", ignoreCase = true)) {
                    if (currentSection.isNotEmpty()) {
                        map[currentSection] = currentContent.toString().trim()
                        currentContent.clear()
                    }
                    currentSection = sec
                    matched = true
                    // Strip the tag
                    val cleaned = line.replace(Regex("^\\[?$sec\\]?:?\\s*", RegexOption.IGNORE_CASE), "")
                    if (cleaned.isNotEmpty()) {
                        currentContent.append(cleaned).append("\n")
                    }
                    break
                }
            }
            if (!matched && currentSection.isNotEmpty()) {
                currentContent.append(line).append("\n")
            }
        }
        if (currentSection.isNotEmpty()) {
            map[currentSection] = currentContent.toString().trim()
        }

        // Fill defaults if empty
        if (map["SLOGAN"].isNullOrEmpty()) map["SLOGAN"] = "आपला व्यवसाय... आपला ब्रँड... आपला अभिमान!"
        if (map["CAPTION"].isNullOrEmpty()) map["CAPTION"] = "Join us and grow! Proudly supporting Vidarbha."
        if (map["REEL_SCRIPT"].isNullOrEmpty()) map["REEL_SCRIPT"] = "Show your business to the world! Vidarbha power."
        if (map["KEYWORDS"].isNullOrEmpty()) map["KEYWORDS"] = "#VDB #Vidarbha #LocalBusiness #MakeInMaharashtra"

        return map
    }

    // Place product order & trigger simulated delivery updates
    fun placeOrder(business: Business, product: Product, address: String) {
        viewModelScope.launch {
            val orderId = repository.insertOrder(
                Order(
                    businessId = business.id,
                    businessName = business.name,
                    productName = product.name,
                    price = product.price,
                    status = "PENDING",
                    deliveryAddress = address,
                    deliveryType = when (business.category) {
                        "Restaurants", "Street Food", "Cloud Kitchen" -> "Food"
                        "Pharmacy", "Medical" -> "Medicine"
                        "Groceries" -> "Grocery"
                        else -> "Parcel"
                    }
                )
            ).toInt()

            // Simulation loop
            viewModelScope.launch {
                delay(8000)
                repository.updateOrderStatus(orderId, "ASSIGNED")
                delay(8000)
                repository.updateOrderStatus(orderId, "OUT_FOR_DELIVERY")
                delay(12000)
                repository.updateOrderStatus(orderId, "DELIVERED")
            }
        }
    }

    // Vendor: Create a new listing
    fun registerNewBusiness(name: String, desc: String, category: String, phone: String, email: String, address: String) {
        viewModelScope.launch {
            val newBiz = Business(
                name = name,
                description = desc,
                descriptionMr = desc,
                descriptionHi = desc,
                category = category,
                phone = phone,
                email = email,
                address = address,
                isVerified = true,
                rating = 4.8f,
                reviewCount = 1,
                ownerEmail = "vendor@vdb.com"
            )
            val bizId = repository.insertBusiness(newBiz).toInt()

            // Seed 2 default products for it
            repository.insertProduct(
                Product(
                    businessId = bizId,
                    name = "Premium Product / Service 1",
                    nameMr = "प्रीमियम उत्पादन १",
                    price = 499.00,
                    description = "Premium service/product package curated by $name."
                )
            )
            repository.insertProduct(
                Product(
                    businessId = bizId,
                    name = "Consultation Offer",
                    nameMr = "सल्लामसलत ऑफर",
                    price = 999.00,
                    description = "Specialized launch package available for limited customers."
                )
            )
        }
    }

    // Database Seed Logic
    private fun seedInitialData() {
        viewModelScope.launch {
            repository.allBusinesses.first().let { current ->
                if (current.isEmpty()) {
                    // Seed premium Vidarbha businesses
                    val seeds = listOf(
                        Business(
                            name = "Nagpur Sweet Orange Orchards",
                            description = "Organic juicy Nagpur Oranges direct from the rich black soils of Katol. Freshly handpicked.",
                            descriptionMr = "काटोलच्या काळ्या मातीतील सेंद्रिय रसाळ नागपुरी संत्री थेट बागेतून. ताजी आणि स्वादिष्ट.",
                            descriptionHi = "काटोल की समृद्ध काली मिट्टी से सीधे जैविक रसीले नागपुरी संतरे। सीधे बगीचे से ताजा।",
                            category = "Agriculture",
                            phone = "+91 98765 11111",
                            email = "oranges@vdb.com",
                            address = "Katol Road, Nagpur, Vidarbha",
                            isVerified = true,
                            rating = 4.9f,
                            reviewCount = 48
                        ),
                        Business(
                            name = "Hotel Saoji Varhadi Spices",
                            description = "Experience Nagpur's legendary spicy Saoji and Varhadi traditional non-veg and veg meals.",
                            descriptionMr = "नागपूरचे सुप्रसिद्ध मसालेदार सावजी आणि वऱ्हाडी पारंपरिक जेवण. अस्सल विदर्भीय चव.",
                            descriptionHi = "नागपुर का प्रसिद्ध मसालेदार सावजी और वऱ्हाडी पारंपरिक शाकाहारी और मांसाहारी भोजन।",
                            category = "Restaurants",
                            phone = "+91 98765 22222",
                            email = "saojikitchen@vdb.com",
                            address = "Deo Nagar, Nagpur",
                            isVerified = true,
                            rating = 4.8f,
                            reviewCount = 120
                        ),
                        Business(
                            name = "Bhandara Pure Silk Weavers Guild",
                            description = "Authentic premium Tussar silk sarees and Kosa silk handlooms handmade by generational artists.",
                            descriptionMr = "पिढ्यानपिढ्या काम करणाऱ्या कलाकारांनी तयार केलेल्या अस्सल प्रीमियम टसर आणि कोसा सिल्क साड्या.",
                            descriptionHi = "पीढ़ियों से काम कर रहे बुनकरों द्वारा हस्तनिर्मित असली प्रीमियम टसर और कोसा सिल्क साड़ियाँ।",
                            category = "Artists",
                            phone = "+91 98765 33333",
                            email = "kosasilk@vdb.com",
                            address = "Weavers Colony, Bhandara",
                            isVerified = true,
                            rating = 4.7f,
                            reviewCount = 35
                        ),
                        Business(
                            name = "Dhanwantari Ayurvedic Pharmacy",
                            description = "100% pure organic herbal remedies, Ayurvedic healthcare supplements and traditional medicines.",
                            descriptionMr = "१००% शुद्ध सेंद्रिय आयुर्वेदिक औषधोपचार आणि पारंपरिक आरोग्यवर्धक औषधे.",
                            descriptionHi = "100% शुद्ध जैविक आयुर्वेदिक जड़ी-बूटियाँ और पारंपरिक स्वास्थ्य सप्लीमेंट्स।",
                            category = "Medical",
                            phone = "+91 98765 44444",
                            email = "dhanwantari@vdb.com",
                            address = "Mul Road, Chandrapur",
                            isVerified = true,
                            rating = 4.6f,
                            reviewCount = 28
                        ),
                        Business(
                            name = "Narmada Green Meadows Wardha",
                            description = "Environmentally conscious sustainable premium housing properties, plots, and real estate consultants.",
                            descriptionMr = "पर्यावरणपूरक आणि शाश्वत प्रीमियम निवासी प्लॉट आणि भव्य घरे.",
                            descriptionHi = "पर्यावरण के अनुकूल और टिकाऊ प्रीमियम आवासीय प्लॉट और सुंदर घर।",
                            category = "Real Estate",
                            phone = "+91 98765 55555",
                            email = "narmada@vdb.com",
                            address = "Sevagram Road, Wardha",
                            isVerified = false,
                            rating = 4.5f,
                            reviewCount = 15
                        )
                    )

                    seeds.forEach { b ->
                        val bId = repository.insertBusiness(b).toInt()
                        
                        // Seed specific products for each
                        when (b.category) {
                            "Agriculture" -> {
                                repository.insertProduct(Product(businessId = bId, name = "A-Grade Nagpur Oranges (5kg Box)", nameMr = "उत्कृष्ट दर्जाचे नागपूर संत्री (५ किलो)", price = 349.00, description = "Freshly harvested. Naturally sweet and high in Vitamin C.", imageType = "orange"))
                                repository.insertProduct(Product(businessId = bId, name = "Organic Orange Blossom Honey (250g)", nameMr = "सेंद्रिय संत्रा फुलांचा मध (२५० ग्रॅम)", price = 220.00, description = "Pure raw honey harvested from orange groves.", imageType = "honey"))
                            }
                            "Restaurants" -> {
                                repository.insertProduct(Product(businessId = bId, name = "Special Varhadi Chicken Thali", nameMr = "विशेष वऱ्हाडी चिकन थाळी", price = 240.00, description = "Authentic rich curry served with traditional Jowar Bhakri.", imageType = "food"))
                                repository.insertProduct(Product(businessId = bId, name = "Saoji Spice Masala Packet (200g)", nameMr = "सावजी स्पेशल मसाला (२०० ग्रॅम)", price = 120.00, description = "Our secret recipe of 24 traditional toasted spices.", imageType = "spice"))
                            }
                            "Artists" -> {
                                repository.insertProduct(Product(businessId = bId, name = "Royal Gold Kosa Silk Saree", nameMr = "शाही सुवर्ण कोसा सिल्क साडी", price = 5499.00, description = "100% handwoven with pure natural silk. Royal gold borders.", imageType = "handloom"))
                                repository.insertProduct(Product(businessId = bId, name = "Tussar Silk Dupatta", nameMr = "टसर सिल्क दुपट्टा", price = 1250.00, description = "Elegant designer dupatta reflecting traditional Bhandara patterns.", imageType = "handloom"))
                            }
                            "Medical" -> {
                                repository.insertProduct(Product(businessId = bId, name = "Pure Triphala Churna (100g)", nameMr = "शुद्ध त्रिफळा चूर्ण (१०० ग्रॅम)", price = 95.00, description = "Promotes digestive wellness and general body immunity.", imageType = "medical"))
                                repository.insertProduct(Product(businessId = bId, name = "Vidarbha Forest Honey Cure", nameMr = "विदर्भ वन मध", price = 180.00, description = "Sourced directly from dense Tadoba forests.", imageType = "medical"))
                            }
                            "Real Estate" -> {
                                repository.insertProduct(Product(businessId = bId, name = "Sevagram Road Commercial Plot", nameMr = "सेवाग्राम रोड व्यावसायिक प्लॉट", price = 1500000.00, description = "East-facing layout ideal for shops, clinics, or showrooms.", imageType = "generic"))
                            }
                        }
                    }
                }
                
                // Add first AI logs if empty
                repository.aiLogs.first().let { logs ->
                    if (logs.isEmpty()) {
                        repository.insertAdvice(AIAdvice(sender = "AI", messageText = "Namaskar! I am your Vidarbha Digital Bajaar AI Growth Advisor. How can I assist you in scaling your business digitally today? Let's discuss marketing, SEO, and sales scaling!"))
                    }
                }
            }
        }
    }
}
