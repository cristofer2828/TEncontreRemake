package com.example.teencontre.actividades

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teencontre.R
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import com.example.teencontre.ui.theme.TeEncontreTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.example.teencontre.sharedprefs.PreferenceManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import com.example.teencontre.data.DatabaseHelper
import com.example.teencontre.data.MascotasAdopcionModel
import com.example.teencontre.data.MascotasEncontradasModel
import com.example.teencontre.data.MascotasPerdidasModel
import com.example.teencontre.screens.EditPerdidoScreen
import com.example.teencontre.screens.EditEncontradaScreen
import com.example.teencontre.screens.EditAdopcionScreen
import com.example.teencontre.screens.WizardCrearAnuncio
import com.example.teencontre.screens.WizardEncontreAnuncio
import com.example.teencontre.screens.WizardCrearAdopcion
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current

            // Inicializamos PreferenceManager una sola vez para toda la App
            val prefs = remember { PreferenceManager(context) }
            // Estados globales de navegación y configuración
            var currentScreen by remember { mutableStateOf("login") }
            var selectedMascotaId by remember { mutableStateOf(0) }
            var wizardMode by remember { mutableStateOf("perdi") }
            var isDarkMode by remember { mutableStateOf(false) }

            TeEncontreTheme(darkTheme = isDarkMode, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Crossfade(targetState = currentScreen, label = "main_navigation") { screen ->
                        when (screen) {
                            "login" -> LoginScreen(
                                onLoginSuccess = { currentScreen = "selector" },
                                onRegisterClick = { currentScreen = "register" }
                            )
                            "register" -> RegisterScreen(
                                onRegisterSuccess = { currentScreen = "login" },
                                onBackToLogin = { currentScreen = "login" },
                                onShowTerms = { isOns ->
                                    currentScreen = if (isOns) "terms_ons" else "terms_user"
                                }
                            )
                            "selector" -> {
                                CreateAnnouncementScreen(
                                    onEncontreClick = {
                                        wizardMode = "encontre"
                                        currentScreen = "wizard"
                                    },
                                    onPerdiClick = {
                                        wizardMode = "perdi"
                                        currentScreen = "wizard"
                                    },
                                    onAdopcionClick = {
                                        wizardMode = "adopcion"
                                        currentScreen = "wizard"
                                    },
                                    onProfileClick = { currentScreen = "profile" },
                                    onPublishClick = { currentScreen = "selector" },
                                    onNavigate = { currentScreen = it }
                                )
                            }
                            "wizard" -> {
                                when (wizardMode) {
                                    "perdi" -> WizardCrearAnuncio(
                                        onBackToSelector = { currentScreen = "selector" }
                                    )
                                    "encontre" -> WizardEncontreAnuncio(
                                        onBackToSelector = { currentScreen = "selector" }
                                    )
                                    "adopcion" -> WizardCrearAdopcion(
                                        onBackToSelector = { currentScreen = "selector" }
                                    )
                                }
                            }
                            "profile" -> ProfileScreen(
                                prefs = prefs,
                                onLogout = { currentScreen = "login" },
                                onNavigate = { route ->
                                    // CORRECCIÓN: Lógica inteligente para interceptar rutas dinámicas con ids
                                    when {
                                        route.startsWith("editar_perdido/") -> {
                                            val idStr = route.substringAfter("editar_perdido/")
                                            selectedMascotaId = idStr.toIntOrNull() ?: 0
                                            currentScreen = "editar_perdido"
                                        }
                                        route.startsWith("editar_encontrada/") -> {
                                            val idStr = route.substringAfter("editar_encontrada/")
                                            selectedMascotaId = idStr.toIntOrNull() ?: 0
                                            currentScreen = "editar_encontrada"
                                        }
                                        route.startsWith("editar_adopcion/") -> {
                                            val idStr = route.substringAfter("editar_adopcion/")
                                            selectedMascotaId = idStr.toIntOrNull() ?: 0
                                            currentScreen = "editar_adopcion"
                                        }
                                        else -> currentScreen = route
                                    }
                                }
                            )

                            // CORRECCIÓN: Inyección obligatoria de idMascota a las pantallas de edición
                            "editar_perdido" -> EditPerdidoScreen(
                                idMascota = selectedMascotaId,
                                onBack = { currentScreen = "profile" }
                            )
                            "editar_encontrada" -> EditEncontradaScreen(
                                idMascota = selectedMascotaId,
                                onBack = { currentScreen = "profile" }
                            )
                            "editar_adopcion" -> EditAdopcionScreen(
                                idMascota = selectedMascotaId,
                                onBack = { currentScreen = "profile" }
                            )

                            "settings" -> SettingsScreen(
                                isDarkMode = isDarkMode,
                                onDarkModeChange = { isDarkMode = it },
                                onBack = { currentScreen = "profile" },
                                onNavigate = { route -> currentScreen = route }
                            )
                            "encuentranos" -> EncuentranosScreen(
                                onProfileClick = { currentScreen = "profile" },
                                onPublishClick = { currentScreen = "selector" },
                                onNavigate = { currentScreen = it }
                            )
                            "mapa" -> MapScreen(
                                onNavigate = { currentScreen = it },
                                onProfileClick = { currentScreen = "profile" },
                                onPublishClick = { currentScreen = "selector" }
                            )
                            "detalle_anuncio" -> DetalleAnuncioScreen(
                                onBack = { currentScreen = "encuentranos" },
                                onNavigate = { currentScreen = it }
                            )
                            "terms_user" -> TermsFrame(
                                title = "Términos de Usuario",
                                content = textoTerminosUsuario,
                                onBack = { currentScreen = "register" }
                            )
                            "terms_ons" -> TermsFrame(
                                title = "Términos para Organizaciones",
                                content = textoTerminosONS,
                                onBack = { currentScreen = "register" }
                            )
                        }
                    }
                }
            }
        }
    }
}


// --- PANTALLA: LOGIN ---
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onRegisterClick: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary

    // 1. ESTADOS PARA LOS INPUTS (Para que permitan escribir)
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // 2. ESTADO PARA LA VISIBILIDAD DE LA CONTRASEÑA
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        Box(
            modifier = Modifier
                .size(250.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_perros),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Ingresar",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        LoginInput(
            label = "Correo",
            value = correo,
            onValueChange = { correo = it },
            placeholder = "example@email.com"
        )

        Spacer(modifier = Modifier.height(16.dp))

        LoginInput(
            label = "Contraseña",
            value = password,
            onValueChange = { password = it },
            placeholder = "Contraseña",
            isPassword = !passwordVisible,
            trailingIcon = {
                val icon = if (passwordVisible) android.R.drawable.ic_menu_view else android.R.drawable.ic_menu_close_clear_cancel
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = "Ver contraseña",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = onLoginSuccess,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
        ) {
            Text("Ingresar")
        }

        TextButton(onClick = onRegisterClick) {
            Text("Registrar", color = primaryColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    onShowTerms: (Boolean) -> Unit
) {
    val primaryPurple = Color(0xFF7C4DFF)
    val linkBlue = Color(0xFF2196F3)

    // ESTADOS
    var isOrganization by remember { mutableStateOf(false) }
    var acceptedTerms by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // TÍTULO DINÁMICO
        Text(
            text = if (isOrganization) "Registrar ONS" else "Registrar",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        // SWITCH DE VERIFICACIÓN ONS
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = isOrganization,
                onCheckedChange = { isOrganization = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Verifíquese como centro de adopción", fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // FORMULARIO DINÁMICO
        if (isOrganization) {
            LoginInput("Nombre", "", {}, "nombre de la organización")
            Spacer(modifier = Modifier.height(16.dp))
            LoginInput("Número de teléfono", "", {}, "+51")
            Spacer(modifier = Modifier.height(16.dp))
            LoginInput("Correo", "", {}, "example@email.com")
            Spacer(modifier = Modifier.height(16.dp))
            LoginInput("RUC", "", {}, "RUC")
            Spacer(modifier = Modifier.height(16.dp))
            LoginInput("Dirección", "", {}, "Dirección")
        } else {
            LoginInput("Nombres", "", {}, "Su nombre")
            Spacer(modifier = Modifier.height(16.dp))
            LoginInput("Número de teléfono", "", {}, "+51")
            Spacer(modifier = Modifier.height(16.dp))
            LoginInput("Correo", "", {}, "example@email.com")
        }

        // CAMPOS COMUNES
        Spacer(modifier = Modifier.height(16.dp))
        LoginInput("Contraseña", "", {}, "Contraseña", isPassword = true)
        Spacer(modifier = Modifier.height(16.dp))
        LoginInput("Confirmar contraseña", "", {}, "Confirmar contraseña", isPassword = true)

        // MENSAJE DE ESPERA PARA ONS
        if (isOrganization) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "confirmación tardará máximo 1 semana.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        // TÉRMINOS Y CONDICIONES CON NAVEGACIÓN
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = acceptedTerms,
                onCheckedChange = { acceptedTerms = it },
                modifier = Modifier.scale(0.7f)
            )

            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = if (isOrganization) "Acepto los términos para organizaciones." else "Acepto los términos y condiciones.",
                    fontSize = 12.sp
                )
                TextButton(
                    onClick = { onShowTerms(isOrganization) }, // Abre el frame correspondiente
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(20.dp)
                ) {
                    Text(
                        text = "Más información",
                        color = linkBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // BOTÓN REGISTRAR
        Button(
            onClick = onRegisterSuccess,
            enabled = acceptedTerms,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryPurple)
        ) {
            Text("Registrar", fontWeight = FontWeight.Bold)
        }

        TextButton(onClick = onBackToLogin) {
            Text("¿Ya tienes cuenta? Ingresa aquí.", color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun TermsFrame(title: String, content: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(text = title, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = content, fontSize = 15.sp, color = Color.DarkGray, lineHeight = 20.sp)
        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
        ) {
            Text("Entendido y volver", fontWeight = FontWeight.Bold)
        }
    }
}

val textoTerminosUsuario = """
    1. USO RESPONSABLE: Esta plataforma es exclusivamente para facilitar la adopción y el reencuentro de mascotas.
    
    2. DATOS PERSONALES: Al registrarte, aceptas que tus datos de contacto sean visibles para otros usuarios cuando reportes o busques una mascota.
    
    3. PROHIBICIONES: Está estrictamente prohibido lucrar o vender animales a través de esta aplicación.
    
    4. COMUNIDAD: Nos reservamos el derecho de eliminar cuentas que realicen reportes falsos.
""".trimIndent()

val textoTerminosONS = """
    1. VERIFICACIÓN DE IDENTIDAD: Como organización, es obligatorio proporcionar un RUC válido.
    
    2. TIEMPO DE RESPUESTA: La confirmación de tu centro de adopción tardará un máximo de 1 semana mientras validamos tus datos.
    
    3. COMPROMISO: Te comprometes a garantizar el bienestar de los animales publicados y a mantener la información actualizada.
    
    4. PRIVACIDAD: Tu dirección y RUC serán almacenados de forma segura y usados solo para fines de transparencia institucional.
""".trimIndent()


// --- PANTALLA 2: SELECTOR ---
@Composable
fun CreateAnnouncementScreen(
    onEncontreClick: () -> Unit,
    onPerdiClick: () -> Unit,
    onAdopcionClick: () -> Unit,
    onProfileClick: () -> Unit,
    onPublishClick: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val primaryPurple = MaterialTheme.colorScheme.primary
    var showDialog by remember { mutableStateOf(false) }

    // Estado para habilitar el botón de Adopción (Simulación de Organización)
    var isOrganization by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    onProfileClick = onProfileClick,
                    onPublishClick = onPublishClick,
                    onEncuentranosClick = { onNavigate("encuentranos") },
                    onMapaClick = { onNavigate("mapa") }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Crear un anuncio",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(30.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Switch(
                        checked = isOrganization,
                        onCheckedChange = { isOrganization = it }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Simulación cuenta organización",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                        "¿Qué ha pasado?",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(24.dp))

                // BOTÓN: PERDÍ A MI MASCOTA
                Button(
                    onClick = onPerdiClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryPurple)
                ) {
                    Text("Perdí a mi mascota.", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // BOTÓN: ENCONTRÉ A MI MASCOTA
                OutlinedButton(
                    onClick = onEncontreClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, primaryPurple)
                ) {
                    Text("Encontré mascota", color = primaryPurple, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // BOTÓN: ADOPCIÓN (Bloqueado/Habilitado dinámicamente)
                Button(
                    onClick = { if (isOrganization) onAdopcionClick() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        // Cambia el color a gris si no es organización
                        containerColor = if (isOrganization) primaryPurple else Color.Gray.copy(alpha = 0.6f),
                        contentColor = if (isOrganization) Color.White else Color.DarkGray
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Adopción", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        if (!isOrganization) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_lock_idle_lock),
                                contentDescription = "Bloqueado",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- BURBUJA FLOTANTE ---
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 45.dp, start = 0.dp)
                .size(70.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { showDialog = true },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.adopta),
                contentDescription = "Adopta",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }

    // DIÁLOGO
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    "Adopta con responsabilidad.",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "Adoptar una mascota es un compromiso de amor, cuidado y respeto.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Entendido", color = primaryPurple)
                }
            }
        )
    }
}

@Composable
fun BottomNavigationBar(onProfileClick: () -> Unit, onPublishClick: () -> Unit, onEncuentranosClick: () -> Unit, onMapaClick: () -> Unit) {
    val backgroundColor = MaterialTheme.colorScheme.surface
    val selectedColor = Color(0xFF7C4DFF)
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavigationItem(
            icon = android.R.drawable.ic_menu_add,
            label = "Publicar",
            color = unselectedColor,
            onClick = onPublishClick
        )
        NavigationItem(
            icon = android.R.drawable.ic_menu_search,
            label = "Encuéntralos",
            color = unselectedColor,
            onClick = onEncuentranosClick
        )
        NavigationItem(
            icon = android.R.drawable.ic_dialog_map,
            label = "Mapa",
            color = unselectedColor,
            onClick = onMapaClick
        )
        NavigationItem(
            icon = android.R.drawable.ic_menu_myplaces,
            label = "Perfil",
            color = selectedColor,
            onClick = onProfileClick
        )
    }
}

@Composable
fun NavigationItem(icon: Int, label: String, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(text = label, fontSize = 10.sp, color = color)
    }
}

@Composable
fun LoginInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val customKeyboardOptions = if (isPassword) {
        keyboardOptions.copy(keyboardType = KeyboardType.Password)
    } else {
        keyboardOptions
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = customKeyboardOptions,
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
@Composable
fun ProfileScreen(
    prefs: PreferenceManager,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }

    // Listas de estados para almacenar las publicaciones leídas desde SQLite
    var listaPerdidos by remember { mutableStateOf<List<MascotasPerdidasModel>>(emptyList()) }
    var listaEncontrados by remember { mutableStateOf<List<MascotasEncontradasModel>>(emptyList()) }
    var listaAdopciones by remember { mutableStateOf<List<MascotasAdopcionModel>>(emptyList()) }

    // Estado clave de control para forzar la recarga cuando eliminamos o entramos a la pantalla
    var refreshTrigger by remember { mutableIntStateOf(0) }

    // CORRECCIÓN CRÍTICA: Cada vez que 'refreshTrigger' cambie, se volverá a consultar SQLite de inmediato
    LaunchedEffect(key1 = refreshTrigger) {
        val db = dbHelper.readableDatabase

        // 1. Leer Perdidos
        val listaP = mutableListOf<MascotasPerdidasModel>()
        val cursorP = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_PERDIDOS}", null)
        if (cursorP.moveToFirst()) {
            do {
                listaP.add(
                    MascotasPerdidasModel(
                        id = cursorP.getInt(cursorP.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_ID)),
                        nombreM = cursorP.getString(cursorP.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_NOMBRE)),
                        especie = cursorP.getString(cursorP.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_ESPECIE)),
                        genero = cursorP.getString(cursorP.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_GENERO)),
                        raza = cursorP.getString(cursorP.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_RAZA)),
                        foto = cursorP.getBlob(cursorP.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_FOTO)),
                        fecha = cursorP.getString(cursorP.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_FECHA)),
                        lugar = cursorP.getString(cursorP.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_LUGAR)),
                        descripcion = cursorP.getString(cursorP.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_DESCRIPCION)),
                        contacto = cursorP.getString(cursorP.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_CONTACTO)),
                        telefono = cursorP.getString(cursorP.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_TELEFONO)),
                        correo = cursorP.getString(cursorP.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_CORREO))
                    )
                )
            } while (cursorP.moveToNext())
        }
        cursorP.close()
        listaPerdidos = listaP

        // 2. Leer Encontrados
        val listaE = mutableListOf<MascotasEncontradasModel>()
        val cursorE = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_ENCONTRADOS}", null)
        if (cursorE.moveToFirst()) {
            do {
                listaE.add(
                    MascotasEncontradasModel(
                        id = cursorE.getInt(cursorE.getColumnIndexOrThrow(DatabaseHelper.ENCONTRADO_ID)),
                        especie = cursorE.getString(cursorE.getColumnIndexOrThrow(DatabaseHelper.ENCONTRADO_ESPECIE)),
                        genero = cursorE.getString(cursorE.getColumnIndexOrThrow(DatabaseHelper.ENCONTRADO_GENERO)),
                        foto = cursorE.getBlob(cursorE.getColumnIndexOrThrow(DatabaseHelper.ENCONTRADO_FOTO)),
                        fecha = cursorE.getString(cursorE.getColumnIndexOrThrow(DatabaseHelper.ENCONTRADO_FECHA)),
                        lugar = cursorE.getString(cursorE.getColumnIndexOrThrow(DatabaseHelper.ENCONTRADO_LUGAR)),
                        descripcion = cursorE.getString(cursorE.getColumnIndexOrThrow(DatabaseHelper.ENCONTRADO_DESCRIPCION)),
                        contacto = cursorE.getString(cursorE.getColumnIndexOrThrow(DatabaseHelper.ENCONTRADO_CONTACTO)),
                        telefono = cursorE.getString(cursorE.getColumnIndexOrThrow(DatabaseHelper.ENCONTRADO_TELEFONO)),
                        correo = cursorE.getString(cursorE.getColumnIndexOrThrow(DatabaseHelper.ENCONTRADO_CORREO))
                    )
                )
            } while (cursorE.moveToNext())
        }
        cursorE.close()
        listaEncontrados = listaE

        // 3. Leer Adopciones
        val listaA = mutableListOf<MascotasAdopcionModel>()
        val cursorA = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_ADOPCION}", null)
        if (cursorA.moveToFirst()) {
            do {
                listaA.add(
                    MascotasAdopcionModel(
                        id = cursorA.getInt(cursorA.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_ID)),
                        especie = cursorA.getString(cursorA.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_ESPECIE)),
                        genero = cursorA.getString(cursorA.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_GENERO)),
                        raza = cursorA.getString(cursorA.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_RAZA)),
                        vacunado = cursorA.getInt(cursorA.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_VACUNADO)) == 1,
                        esterilizado = cursorA.getInt(cursorA.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_ESTERILIZADO)) == 1,
                        desparasitado = cursorA.getInt(cursorA.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_DESPARASITADO)) == 1,
                        tamano = cursorA.getString(cursorA.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_TAMANO)),
                        temperamento = cursorA.getString(cursorA.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_TEMPERAMENTO)),
                        foto = cursorA.getBlob(cursorA.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_FOTO)),
                        descripcion = cursorA.getString(cursorA.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_DESCRIPCION)),
                        nombreOrganizacion = cursorA.getString(cursorA.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_ORGANIZACION)),
                        telefono = cursorA.getString(cursorA.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_TELEFONO)),
                        correo = cursorA.getString(cursorA.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_CORREO))
                    )
                )
            } while (cursorA.moveToNext())
        }
        cursorA.close()
        listaAdopciones = listaA
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                onProfileClick = { refreshTrigger++ }, // Forzar actualización manual si presionan de nuevo su propio botón
                onPublishClick = { onNavigate("selector") },
                onEncuentranosClick = { onNavigate("encuentranos") },
                onMapaClick = { onNavigate("mapa") }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "Mi perfil",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))
            ProfileOptionsCard(onNavigate = onNavigate)
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Mis anuncios",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            val totalAnuncios = listaPerdidos.size + listaEncontrados.size + listaAdopciones.size

            if (totalAnuncios > 0) {
                // ---- Renderizar Mascotas Perdidas ----
                listaPerdidos.forEach { mascota ->
                    AdItemCard(
                        description = mascota.nombreM,
                        status = "PERDIDA",
                        location = mascota.lugar,
                        fotoBytes = mascota.foto,
                        onEdit = { onNavigate("editar_perdido/${mascota.id}") },
                        onDelete = {
                            val db = dbHelper.writableDatabase
                            db.delete(DatabaseHelper.TABLE_PERDIDOS, "${DatabaseHelper.PERDIDO_ID} = ?", arrayOf(mascota.id.toString()))
                            refreshTrigger++ // Lanza la actualización reactiva
                        }
                    )
                }

                // ---- Renderizar Mascotas Encontradas ----
                listaEncontrados.forEach { mascota ->
                    AdItemCard(
                        description = "Especie: ${mascota.especie}",
                        status = "ENCONTRADA",
                        location = mascota.lugar,
                        fotoBytes = mascota.foto,
                        onEdit = { onNavigate("editar_encontrada/${mascota.id}") },
                        onDelete = {
                            val db = dbHelper.writableDatabase
                            db.delete(DatabaseHelper.TABLE_ENCONTRADOS, "${DatabaseHelper.ENCONTRADO_ID} = ?", arrayOf(mascota.id.toString()))
                            refreshTrigger++ // Lanza la actualización reactiva
                        }
                    )
                }

                // ---- Renderizar Casos de Adopción ----
                listaAdopciones.forEach { mascota ->
                    AdItemCard(
                        description = "${mascota.especie} (${mascota.raza})",
                        status = "ADOPCION",
                        location = mascota.nombreOrganizacion,
                        fotoBytes = mascota.foto,
                        onEdit = { onNavigate("editar_adopcion/${mascota.id}") },
                        onDelete = {
                            val db = dbHelper.writableDatabase
                            db.delete(DatabaseHelper.TABLE_ADOPCION, "${DatabaseHelper.ADOPCION_ID} = ?", arrayOf(mascota.id.toString()))
                            refreshTrigger++ // Lanza la actualización reactiva
                        }
                    )
                }
            } else {
                NoAdsCard()
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(200.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD96666)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cerrar sesión", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun NoAdsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Aún no tienes anuncios publicados",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AdItemCard(
    description: String,
    status: String,
    location: String,
    fotoBytes: ByteArray?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (fotoBytes != null && fotoBytes.isNotEmpty()) {
                    val bitmap = remember(fotoBytes) {
                        android.graphics.BitmapFactory.decodeByteArray(fotoBytes, 0, fotoBytes.size)
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Foto mascota",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    Text("🐾", fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = description,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = status.uppercase(),
                    color = when (status.uppercase()) {
                        "PERDIDA" -> Color(0xFF7C4DFF)
                        "ENCONTRADA" -> Color(0xFF4CAF50)
                        "ADOPCION", "ADOPCION" -> Color(0xFF2196F3)
                        else -> Color(0xFF7C4DFF)
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = location,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { onEdit() },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Editar anuncio",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                IconButton(
                    onClick = { onDelete() },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Eliminar anuncio",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileOptionItem(icon: Int, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }

    // DATOS DE CONTACTO
    var nombre by remember { mutableStateOf(prefs.getUserName()) }
    var telefono by remember { mutableStateOf(prefs.getPhone()) }
    var correo by remember { mutableStateOf(prefs.getEmail()) }
    var emailNotifications by remember { mutableStateOf(prefs.getNotifications()) }

    // DATOS DE SEGURIDAD
    var passwordActual by remember { mutableStateOf("") }
    var passwordNueva by remember { mutableStateOf("") }
    var passwordRepetir by remember { mutableStateOf("") }

    // ESTADOS DE VISIBILIDAD DE CONTRASEÑA
    var showPasswordActual by remember { mutableStateOf(false) }
    var showPasswordNueva by remember { mutableStateOf(false) }
    var showPasswordRepetir by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                onProfileClick = { onBack() },
                onPublishClick = { onNavigate("selector") },
                onEncuentranosClick = { onNavigate("encuentranos") },
                onMapaClick = { onNavigate("mapa") }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // BOTÓN ATRÁS
            TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("← Atrás", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                }
            }

            Text(
                text = "Ajustes",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // MODO OSCURO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Modo oscuro", fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground)
                Switch(checked = isDarkMode, onCheckedChange = { onDarkModeChange(it) })
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)

            // SECCIÓN: INFORMACIÓN DE CONTACTO
            Text("Información del contacto", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))

            LoginInput("Su nombre", nombre, { nombre = it }, "Nombre")
            Spacer(modifier = Modifier.height(12.dp))
            LoginInput("Número de teléfono", telefono, { if (it.length <= 9) telefono = it }, "+51")
            Spacer(modifier = Modifier.height(12.dp))
            LoginInput("Tu correo electrónico", correo, { correo = it }, "example@email.com")

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = emailNotifications,
                    onCheckedChange = { emailNotifications = it; prefs.setNotifications(it) },
                    modifier = Modifier.scale(0.8f)
                )
                Text("Notificaciones por correo", fontSize = 14.sp)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)

            // SECCIÓN: SEGURIDAD
            Text("Seguridad", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))

            // Campo 1: Contraseña Actual
            LoginInput(
                label = "Tu contraseña actual",
                value = passwordActual,
                onValueChange = { passwordActual = it },
                placeholder = "Contraseña",
                isPassword = !showPasswordActual,
                trailingIcon = {
                    val icon = if (!showPasswordActual) android.R.drawable.ic_menu_view else android.R.drawable.ic_menu_close_clear_cancel
                    IconButton(onClick = { showPasswordActual = !showPasswordActual }) {
                        Icon(painter = painterResource(id = icon), contentDescription = null, modifier = Modifier.size(24.dp))
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Campo 2: Nueva Contraseña
            LoginInput(
                label = "Ingrese una nueva contraseña.",
                value = passwordNueva,
                onValueChange = { passwordNueva = it },
                placeholder = "Nueva contraseña",
                isPassword = !showPasswordNueva,
                trailingIcon = {
                    val icon = if (!showPasswordNueva) android.R.drawable.ic_menu_view else android.R.drawable.ic_menu_close_clear_cancel
                    IconButton(onClick = { showPasswordNueva = !showPasswordNueva }) {
                        Icon(painter = painterResource(id = icon), contentDescription = null, modifier = Modifier.size(24.dp))
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Campo 3: Repetir Contraseña
            LoginInput(
                label = "Repita la nueva contraseña.",
                value = passwordRepetir,
                onValueChange = { passwordRepetir = it },
                placeholder = "Nueva contraseña",
                isPassword = !showPasswordRepetir,
                trailingIcon = {
                    val icon = if (!showPasswordRepetir) android.R.drawable.ic_menu_view else android.R.drawable.ic_menu_close_clear_cancel
                    IconButton(onClick = { showPasswordRepetir = !showPasswordRepetir }) {
                        Icon(painter = painterResource(id = icon), contentDescription = null, modifier = Modifier.size(24.dp))
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    prefs.setUserName(nombre); prefs.setPhone(telefono); prefs.setEmail(correo)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("APLICAR", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
@Composable
fun ProfileOptionsCard(onNavigate: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 20.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileOptionItem(
                icon = android.R.drawable.ic_menu_preferences,
                label = "Ajustes",
                onClick = { onNavigate("settings") }
            )

            ProfileOptionItem(
                icon = android.R.drawable.ic_input_add,
                label = "Crear",
                onClick = { onNavigate("selector") }
            )
        }
    }
}

