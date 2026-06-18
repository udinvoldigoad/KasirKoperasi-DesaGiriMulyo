package com.kasirkoperasi.app.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kasirkoperasi.app.core.navigation.AppRoute
import com.kasirkoperasi.app.ui.theme.DeepGreen
import com.kasirkoperasi.app.ui.theme.FreshMint

@Composable
fun KasirBottomBar(
    selectedRoute: String,
    onRouteSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shadowElevation = 10.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomItem(
                label = AppRoute.Home.title,
                selected = selectedRoute == AppRoute.Home.route,
                icon = Icons.Outlined.Home,
                onClick = { onRouteSelected(AppRoute.Home.route) },
                modifier = Modifier.weight(1f),
            )
            BottomItem(
                label = AppRoute.Transaction.title,
                selected = selectedRoute == AppRoute.Transaction.route,
                icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                onClick = { onRouteSelected(AppRoute.Transaction.route) },
                modifier = Modifier.weight(1f),
            )
            BottomItem(
                label = AppRoute.Product.title,
                selected = selectedRoute == AppRoute.Product.route,
                icon = Icons.Outlined.Inventory2,
                onClick = { onRouteSelected(AppRoute.Product.route) },
                modifier = Modifier.weight(1f),
            )
            BottomItem(
                label = AppRoute.Report.title,
                selected = selectedRoute == AppRoute.Report.route,
                icon = Icons.Outlined.BarChart,
                onClick = { onRouteSelected(AppRoute.Report.route) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BottomItem(
    label: String,
    selected: Boolean,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = if (selected) FreshMint else Color.Transparent
    val contentColor = if (selected) DeepGreen else Color(0xFF303A34)

    Surface(
        modifier = modifier
            .height(66.dp)
            .padding(horizontal = 3.dp)
            .clickable { onClick() },
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .background(background, RoundedCornerShape(16.dp))
                .padding(vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(24.dp),
                    tint = contentColor,
                )
            }
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            )
        }
    }
}
