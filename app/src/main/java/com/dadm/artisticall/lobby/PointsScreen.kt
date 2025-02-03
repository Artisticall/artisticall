package com.dadm.artisticall.lobby

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dadm.artisticall.R
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson

@Composable
fun PointsScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E2D36)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.fillMaxSize()
        ) {
            // Screen Title
            Spacer(modifier = Modifier.height(62.dp))
            Text(
                text = "Puntajes",
                fontSize = 28.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(42.dp))
            // Example players JSON
            val playersJson = """
                {
                    "players": [
                        {"name": "Ian", "points": 150},
                        {"name": "Diana", "points": 120},
                        {"name": "Alex", "points": 100},
                        {"name": "Roger", "points": 80},
                        {"name": "Pepe", "points": 50},
                        {"name": "Rosa", "points": 110}
                    ]
                }
            """
            val players = remember(playersJson) {
                val gson = Gson()
                val type = object : TypeToken<Map<String, List<Player>>>() {}.type
                val parsed = gson.fromJson<Map<String, List<Player>>>(playersJson, type)
                parsed["players"] ?: emptyList()
            }.sortedByDescending { it.points }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 36.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Podium Layout
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Second place
                    if (players.size > 1) PlayerCard(players[1], Modifier.weight(1f))

                    // First place (larger with crown)
                    if (players.isNotEmpty()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.corona), // Replace with your crown icon resource
                                contentDescription = "Crown",
                                tint = Color.Yellow,
                                modifier = Modifier
                                    .size(52.dp)
                                    .padding(bottom = 4.dp)
                            )
                            PlayerCard(
                                player = players[0],
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .padding(8.dp)
                            )
                        }
                    }

                    // Third place
                    if (players.size > 2) PlayerCard(players[2], Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Remaining Players List
                // Remaining Players List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    itemsIndexed(players.drop(3)) { index, player ->
                        PlayerListItem(player)

                        // Add a divider between items, but not after the last item
                        if (index < players.drop(3).lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 1.dp,
                                color = Color.White
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            navController.navigate("lobby_screen")
                        },
                    ) {
                        Text(text = "Lobby", fontSize = 16.sp)
                    }
                }
            }

        }
    }
}

@Composable
fun PlayerCard(player: Player, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // User Icon
        Image(
            painter = painterResource(id = R.drawable.ic_user), // Replace with your icon resource
            contentDescription = "Player Icon",
            modifier = Modifier.size(34.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = player.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "${player.points} points",
            fontSize = 14.sp,
            color = Color.White
        )
    }
}

@Composable
fun PlayerListItem(player: Player) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color(0xFF1E2D36))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = player.name,
            fontSize = 16.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "${player.points} points",
            fontSize = 16.sp,
            color = Color.White
        )
    }
}

data class Player(val name: String, val points: Int)


data class Lobby(
    var host: String = "",
    var players: List<Player> = listOf(),
    var winner: String = "",
    var key: String? = null // Firebase key for identifying the game
)
