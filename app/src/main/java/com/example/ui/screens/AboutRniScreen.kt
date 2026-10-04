package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.DarkCardBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.IconGray
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextDisabled
import com.example.ui.theme.TextHighEmphasis
import com.example.ui.theme.TextMediumEmphasis

@Composable
fun AboutRniScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureWhite)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("about_rni_screen")
    ) {
        // Top Official Logo Card
        Surface(
            color = PureWhite,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_red_nacional),
                    contentDescription = "Red Nacional Internet 2.0",
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(70.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Title
        Text(
            text = "Sobre JHTUBE",
            color = TextHighEmphasis,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "JHTUBE — Plataforma de Streaming Ligera y Optimizada",
            color = TextMediumEmphasis,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Card 1: Introducción y Marco Legal
        RniInfoCard(
            title = "Misión y Marco Institucional",
            icon = Icons.Default.Info
        ) {
            Text(
                text = "El Gobierno Nacional a través de la Junta Asesora de Servicio y Acceso Universal, creada mediante la Ley 59 de 11 de agosto de 2008 y administrada por la Autoridad Nacional para la Innovación Gubernamental (AIG), impulsa el proyecto \"Red Nacional Internet\", cuyo objetivo es llevar conectividad inalámbrica a todos los rincones del país.",
                color = TextHighEmphasis,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Como parte de las Estrategias de Modernización del Estado mediante el uso eficaz e intensivo de las TIC incluidas en la Agenda Digital Nacional PANAMÁ 4.0, para el Desarrollo de la Sociedad del Conocimiento y reducción de la Brecha Digital, la Resolución No.14-2015 autorizó la Red Nacional Internet 2.0 (RNI 2.0).",
                color = TextMediumEmphasis,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Card 2: Metas de Cobertura y Capacidad
        RniInfoCard(
            title = "Capacidad y Cobertura Nacional",
            icon = Icons.Default.Speed
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatBadge("1,320", "Puntos Wi-Fi")
                StatBadge("2 Mbps", "Por Usuario")
                StatBadge("288", "Corregimientos")
                StatBadge("+80%", "Población")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "• 60% más de cobertura en sitios de interés público con inversión de B/. 21,600,000 para implementación, mantenimiento y operación.\n" +
                        "• Sitios seleccionados mediante estudio de factibilidad conjunto entre MIDES, ASEP, SENACYT y AIG.\n" +
                        "• Más de 2.9 millones de usuarios registrados, con meta de crecimiento mensual del 40%.",
                color = TextHighEmphasis,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Card 3: Guía de Conexión a InternetParaTodos
        RniInfoCard(
            title = "¿Cómo conectarte a \"InternetParaTodos\"?",
            icon = Icons.Default.Wifi
        ) {
            Text(
                text = "La Red Nacional Internet 2.0 ofrece un servicio de internet gratuito conocido como InternetParaTodos a través de cualquier dispositivo compatible con Wi-Fi (teléfonos inteligentes, computadoras, tabletas).",
                color = TextHighEmphasis,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            StepItem(1, "Activa el Wi-Fi en tu equipo y busca redes disponibles.")
            StepItem(2, "Selecciona la red SSID \"InternetParaTodos\".")
            StepItem(3, "Se abrirá el portal de registro: completa tus datos para identificarte.")
            StepItem(4, "¡Listo! Verás la pantalla de bienvenida y podrás navegar de inmediato.")

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Rango de cobertura óptimo: 50 a 150 metros del Punto de Acceso en espacios abiertos.",
                color = IconGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Card 4: Consejos de Señal y Obstáculos
        RniInfoCard(
            title = "Optimización de Señal y Obstáculos",
            icon = Icons.Default.LocationOn
        ) {
            Text(
                text = "La calidad de señal depende de dos variables principales que puedes controlar:\n\n" +
                        "1. Distancia al punto de acceso: los dispositivos móviles tienen menor potencia de transmisión y requieren estar más cerca.\n" +
                        "2. Obstáculos físicos: paredes, ventanas, árboles e interferencias electromagnéticas (teléfonos inalámbricos u otras redes Wi-Fi) atenúan la señal.",
                color = TextHighEmphasis,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Card 5: Pautas de Navegación Segura
        RniInfoCard(
            title = "Pautas de Navegación y Seguridad",
            icon = Icons.Default.Security
        ) {
            Text(
                text = "Para garantizar un entorno educativo, familiar y seguro, la red incluye filtrado de contenido ofensivo, violencia y descargas potencialmente maliciosas para salvaguardar a los millones de usuarios que acceden diariamente.",
                color = TextHighEmphasis,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Card 6: Atención Ciudadana y Contacto
        RniInfoCard(
            title = "Atención Ciudadana y Reporte de Fallas",
            icon = Icons.Default.Call
        ) {
            Text(
                text = "Si eres usuario de InternetParaTodos y detectas alguna anomalía o daño técnico en un Punto de Acceso:",
                color = TextMediumEmphasis,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            ContactItem(Icons.Default.Call, "Teléfono Gratuito", "800-2121")
            ContactItem(Icons.Default.Language, "Portal Oficial", "www.InternetParaTodos.gob.pa")
            ContactItem(Icons.Default.CheckCircle, "Redes Sociales", "Twitter: @RNIPanama | Facebook: RNIPanama")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun RniInfoCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCardBackground),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, DarkCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = IconGray,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = PureWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun StatBadge(number: String, label: String) {
    Surface(
        color = DarkSurfaceVariant,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, DarkCardBorder),
        modifier = Modifier.padding(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = number, color = PureWhite, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            Text(text = label, color = TextMediumEmphasis, fontSize = 9.sp)
        }
    }
}

@Composable
private fun StepItem(number: Int, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(DarkSurfaceVariant, shape = RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "$number", color = IconGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, color = TextHighEmphasis, fontSize = 12.sp, lineHeight = 16.sp)
    }
}

@Composable
private fun ContactItem(icon: ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = IconGray, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, color = TextMediumEmphasis, fontSize = 10.sp)
            Text(text = value, color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
