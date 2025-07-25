package com.example.chamabuddy.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.chamabuddy.R
import com.example.chamabuddy.domain.model.Cycle
import com.example.chamabuddy.domain.model.Group
import com.example.chamabuddy.presentation.navigation.BeneficiaryGroupDestination
import com.example.chamabuddy.presentation.navigation.BenefitDestination
import com.example.chamabuddy.presentation.navigation.ExpenseDestination
import com.example.chamabuddy.presentation.navigation.GroupsHomeDestination
import com.example.chamabuddy.presentation.navigation.HomeDestination
import com.example.chamabuddy.presentation.navigation.MembersDestination
import com.example.chamabuddy.presentation.navigation.NavigationDestination
import com.example.chamabuddy.presentation.navigation.PenaltyDestination
import com.example.chamabuddy.presentation.navigation.ProfileDestination
import com.example.chamabuddy.presentation.navigation.SavingsDestination
import com.example.chamabuddy.presentation.viewmodel.AuthViewModel
import com.example.chamabuddy.presentation.viewmodel.BenefitViewModel
import com.example.chamabuddy.presentation.viewmodel.CycleEvent
import com.example.chamabuddy.presentation.viewmodel.CycleState
import com.example.chamabuddy.presentation.viewmodel.ExpenseViewModel
import com.example.chamabuddy.presentation.viewmodel.HomeViewModel
import com.example.chamabuddy.presentation.viewmodel.MemberViewModel
import com.example.chamabuddy.presentation.viewmodel.PenaltyViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

val SoftGreen = Color(0xFF4CAF50)
val CardSurface = Color(0xFFFFFFFF)
val SearchBarGray = Color(0xFFF0F0F0)
val PremiumNavy = Color(0xFF0A1D3A)
val SoftOffWhite = Color(0xFFF8F9FA)
val VibrantOrange = Color(0xFFFF6B35)
val ActiveGreen = Color(0xFF4CAF50)
val CompletedGray = Color(0xFF9E9E9E)
val LightAccentBlue = Color(0xFFE3F2FD)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    groupId: String,
    navigateToCycleDetails: (String) -> Unit,
    navigateToCreateCycle: () -> Unit,
    navigateToGroupManagement: () -> Unit,
) {
    val viewModel: HomeViewModel = hiltViewModel()
    var showCreateDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    var bottomBarVisible by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    val userGroups by viewModel.userGroups.collectAsState()
    val showSnackbar by viewModel.showSnackbar.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    val groupData by viewModel.groupData.collectAsState()

    val currentBackStackEntry = navController.currentBackStackEntry
    val savedStateHandle = currentBackStackEntry?.savedStateHandle
    var refreshTrigger by remember { mutableStateOf(0) }

    val totalSavings by viewModel.totalGroupSavings.collectAsState()

    val authViewModel: AuthViewModel = hiltViewModel()
    val currentMemberId by authViewModel.currentMemberId.collectAsState()

    val memberViewModel: MemberViewModel = hiltViewModel()
    val currentUserIsAdmin by memberViewModel.currentUserIsAdmin.collectAsState()
    val currentUserIsOwner by memberViewModel.currentUserIsOwner.collectAsState()

    val expenseViewModel: ExpenseViewModel = hiltViewModel()
    val benefitViewModel: BenefitViewModel = hiltViewModel()
    val penaltyViewModel: PenaltyViewModel = hiltViewModel()




    LaunchedEffect(groupId) {
        if (groupId.isNotEmpty()) {
            authViewModel.loadCurrentMemberId(groupId)
        }
    }

    BackHandler {
        navController.navigate(GroupsHomeDestination.route) {
            // Clear back stack up to Groups Home
            popUpTo(GroupsHomeDestination.route) { inclusive = true }
        }
    }

    val currentUserId by authViewModel.currentUserId.collectAsState()

    LaunchedEffect(groupId, currentUserId) {
        if (groupId.isNotEmpty() && currentUserId != null) {
            val userId = currentUserId!!
            memberViewModel.loadCurrentUserRole(groupId, userId)
        }
    }


    LaunchedEffect(groupId) {
        if (groupId.isNotEmpty()) {
            expenseViewModel.loadData(groupId)
            benefitViewModel.loadData(groupId)
            penaltyViewModel.loadData(groupId)
        }
    }
    val expenseTotal by expenseViewModel.total.collectAsState()
    val benefitTotal by benefitViewModel.total.collectAsState()
    val penaltyTotal by penaltyViewModel.total.collectAsState()


    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.get<Boolean>("cycle_created")?.let { created ->
            if (created) {
                refreshTrigger++ // Force refresh
                savedStateHandle.remove<Boolean>("cycle_created")
            }
        }
    }

    // Add refreshTrigger as key to reload cycles
    LaunchedEffect(groupId, refreshTrigger) {
        if (groupId.isNotEmpty()) {
            viewModel.loadGroupData(groupId)
            viewModel.loadCyclesForGroup(groupId)
        }
    }

    // Show snackbar when needed
    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            snackbarHostState.showSnackbar(snackbarMessage)
            viewModel.resetSnackbar()
        }
    }

    // Drawer state handling
    LaunchedEffect(drawerState) {
        snapshotFlow { drawerState.isOpen }
            .collect { isOpen ->
                bottomBarVisible = !isOpen
            }
    }

    LaunchedEffect(groupId) {
        viewModel.loadUserGroups()
        if (groupId.isNotEmpty()) {
            viewModel.loadGroupData(groupId)
            viewModel.loadCyclesForGroup(groupId)
        } else {
            viewModel.setSnackbarMessage("No group selected. Please select a group first.")
            viewModel.showSnackbar()
        }
    }

    // Scroll detection
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                bottomBarVisible = when {
                    index == 0 && offset == 0 -> true
                    offset > 5 -> false
                    offset < -5 -> true
                    else -> bottomBarVisible
                }
            }
    }


    val stateValue by viewModel.state.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val bottomBarItems = listOf(
        TabItem(icon = Icons.Default.Home, title = "Home", destination = HomeDestination),
        TabItem(icon = Icons.Default.Person, title = "Beneficiaries", destination = BeneficiaryGroupDestination),
        TabItem(icon = Icons.Default.Money, title = "Savings", destination = SavingsDestination),
        TabItem(icon = Icons.Default.Group, title = "Members", destination = MembersDestination),
        TabItem(icon = Icons.Default.AccountCircle, title = "Profile", destination = ProfileDestination)
    )







    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            Box(modifier = Modifier.width(280.dp)) {
                SideNavigationDrawerContent(
                    currentGroupId = groupId,
                    penaltyTotal = penaltyTotal,
                    expenseTotal = expenseTotal,
                    benefitTotal = benefitTotal,
                    groups = userGroups,
                    onGroupSelected = { /* Handle group selection */ },
                    onCreateGroup = { navigateToGroupManagement() },
                    onClose = { scope.launch { drawerState.close() } },
                    onNavToPenalty = {
                        navController.navigate("${PenaltyDestination.route}/$groupId")
                    },
                    onNavToBenefit = {
                        navController.navigate("${BenefitDestination.route}/$groupId")
                    },
                    onNavToExpense = {
                        navController.navigate("${ExpenseDestination.route}/$groupId")
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                LargeTopAppBar(
                    title = {
                        Column {
                            Text(
                                text = groupData?.group?.name ?: "Loading...",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${groupData?.members?.size ?: 0} members",
                                color = Color.White.copy(alpha = 0.8f))
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Navigation Menu",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        Text(
                            text = "KES $totalSavings",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = PremiumNavy,
                        scrolledContainerColor = PremiumNavy,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    ),
                    scrollBehavior = scrollBehavior
                )
            },
            floatingActionButton = {
                if (currentUserIsAdmin || currentUserIsOwner) {
                    FloatingActionButton(
                        onClick = {
                            when {
                                groupId.isEmpty() -> {
                                    viewModel.setSnackbarMessage("Please select a group first")
                                    viewModel.showSnackbar()
                                    navigateToGroupManagement()
                                }
                                userGroups.isEmpty() -> {
                                    viewModel.setSnackbarMessage("No groups available")
                                    viewModel.showSnackbar()
                                }
                                else -> showCreateDialog = true
                            }
                        },
                        containerColor = VibrantOrange
                    ) {
                        Icon(Icons.Default.Add, "New Cycle", tint = Color.White)
                    }
                }
            },
            bottomBar = {
                NavigationBar {
                    bottomBarItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = false,
                            onClick = {
                                when (item.title) {
                                    "Beneficiaries" -> navController.navigate(
                                        "${BeneficiaryGroupDestination.route}/$groupId"
                                    )
                                    "Home" -> navController.navigate("${HomeDestination.route}/$groupId")
                                    "Savings" -> navController.navigate("${SavingsDestination.route}/$groupId")
                                    "Members" -> navController.navigate("${MembersDestination.route}/$groupId")
                                    "Profile" -> {
                                        if (currentMemberId != null) {
                                            navController.navigate("${ProfileDestination.route}/$groupId/$currentMemberId")
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            },
            containerColor = SoftOffWhite
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(PremiumNavy, PremiumNavy.copy(alpha = 0.8f)),
                                startY = 0f,
                                endY = 500f
                            ),
                            shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(1.dp))

                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow))
                    ) {
                        Column {
                            Text(
                                "Hi ${groupData?.group?.adminName ?: "User"}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (stateValue) {
                    is CycleState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = VibrantOrange)
                        }
                    }
                    is CycleState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text((stateValue as CycleState.Error).message, color = PremiumNavy)
                        }
                    }
                    is CycleState.CycleHistory -> {
                        val cycles = (stateValue as CycleState.CycleHistory).cycles

                        val sortedCycles = remember(cycles) {
                            cycles.sortedBy { it.startDate }
                                .mapIndexed { index, cycle ->
                                    cycle.copy(cycleNumber = index + 1)
                                }
                                .sortedByDescending { it.startDate }
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                                .background(Color.White),
                            color = Color.White
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    "All Cycles",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = PremiumNavy,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )

                                if (cycles.isEmpty()) {
                                    EmptyDashboard(navigateToCreateCycle)
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        state = listState,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        items(
                                            items = sortedCycles,
                                            key = { it.cycleId }
                                        ) { cycle ->
                                            PremiumCycleCard(
                                                cycle = cycle,
                                                onViewDetailsClick = { navigateToCycleDetails(cycle.cycleId) },
                                                isAdmin = currentUserIsAdmin || currentUserIsOwner,
                                                onDeleteCycle = { viewModel.deleteCycle(cycle.cycleId) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No cycle data available",
                                color = PremiumNavy
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Start a new cycle?") },
            text = { Text("Are you sure you want to create a new cycle now?") },
            confirmButton = {
                TextButton(onClick = {
                    showCreateDialog = false
                    navigateToCreateCycle()
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PremiumCycleCard(
    cycle: Cycle,
    onDeleteCycle: (() -> Unit)? = null,
    isAdmin: Boolean,
    onViewDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isLongPressed by remember { mutableStateOf(false) }

    val isActive = cycle.endDate == null
    val statusText = if (isActive) "ACTIVE" else "COMPLETED"
    val statusColor = if (isActive) ActiveGreen else CompletedGray
    val endDateDisplay = cycle.endDate?.let { dateFormat.format(it) } ?: "Present"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .combinedClickable(
                onClick = { expanded = !expanded },
                onLongClick = {
                    // FIX: Only show delete dialog if admin and callback provided
                    if (isAdmin && onDeleteCycle != null) {
                        showDeleteDialog = true
                    }
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cycle ${cycle.cycleNumber}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PremiumNavy
                )

                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                text = "ID: ${cycle.cycleId.takeLast(6)}",
                color = PremiumNavy.copy(alpha = 0.4f),
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "${dateFormat.format(cycle.startDate)} - $endDateDisplay",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LightAccentBlue.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FinancialMetric(
                    label = "Total Savings",
                    value = "KES ${cycle.totalSavings}",
                    isPrimary = true
                )
                FinancialMetric(
                    label = "Members",
                    value = "${cycle.totalMembers}",
                    isPrimary = true
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                LightAccentBlue.copy(alpha = 0.1f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FinancialMetric("Monthly", "KES ${cycle.monthlySavingsAmount}", true)
                        FinancialMetric("Weekly", "KES ${cycle.weeklyAmount}", true)
                        FinancialMetric(
                            "Beneficiaries",
                            "${cycle.beneficiariesPerMeeting}/meeting",
                            true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onViewDetailsClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PremiumNavy.copy(alpha = 0.05f),
                            contentColor = PremiumNavy
                        )
                    ) {
                        Text("View Full Details", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    if (isAdmin && showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                isLongPressed = false
            },
            title = { Text("Delete Cycle?") },
            text = { Text("Are you sure you want to permanently delete this cycle?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        isLongPressed = false
                        onDeleteCycle?.invoke()
                    }
                ) {
                    Text("Delete", color = VibrantOrange)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    isLongPressed = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FinancialMetric(
    label: String,
    value: String,
    isPrimary: Boolean
) {
    Column {
        Text(
            text = value,
            fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Medium,
            color = PremiumNavy,
            fontSize = if (isPrimary) 16.sp else 14.sp
        )
        Text(
            text = label,
            color = PremiumNavy.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
    }
}

@Composable
fun MetricItem(
    label: String,
    value: String,
    icon: ImageVector,
    valueFontSize: androidx.compose.ui.unit.TextUnit,
    labelFontSize: androidx.compose.ui.unit.TextUnit,
    spacing: Dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = PremiumNavy.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(spacing))
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            color = PremiumNavy,
            fontSize = valueFontSize
        )
        Text(
            text = label,
            color = PremiumNavy.copy(alpha = 0.6f),
            fontSize = labelFontSize
        )
    }
}

@Composable
fun DetailItem(
    label: String,
    value: String,
    fontSize: androidx.compose.ui.unit.TextUnit
) {
    Column {
        Text(
            text = label,
            color = PremiumNavy.copy(alpha = 0.6f),
            fontSize = fontSize
        )
        Text(
            text = value,
            fontWeight = FontWeight.Medium,
            color = PremiumNavy,
            fontSize = (fontSize.value + 2).sp
        )
    }
}

@Composable
fun EmptyDashboard(onCreateClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.ic_empty_state),
            contentDescription = "No active cycles",
            modifier = Modifier.size(120.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No Active Cycles",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = PremiumNavy,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Start your first cycle and manage your chama efficiently",
            style = MaterialTheme.typography.bodyMedium,
            color = PremiumNavy.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onCreateClick,
            colors = ButtonDefaults.buttonColors(containerColor = VibrantOrange),
            modifier = Modifier.height(50.dp)
        ) {
            Text("Start New Cycle", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SideNavigationDrawerContent(
    currentGroupId: String,
    penaltyTotal: Double,
    expenseTotal: Double,
    benefitTotal: Double,
    onNavToPenalty: () -> Unit,
    onNavToBenefit: () -> Unit,
    onNavToExpense: () -> Unit,
    groups: List<Group>,
    onGroupSelected: (Group) -> Unit,
    onCreateGroup: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PremiumNavy)
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.ic_chama_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape)
            )
        }

        Text(
            "Your Groups",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        if (groups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x44FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "You haven't joined any groups yet",
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onCreateGroup,
                        colors = ButtonDefaults.buttonColors(containerColor = VibrantOrange)
                    ) {
                        Text("Create or Join a Group")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x44FFFFFF))
                    .padding(8.dp)
            ) {
                items(groups) { group ->
                    GroupListItem(group = group, onGroupSelected = onGroupSelected)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        DrawerItem(
            icon = Icons.Default.MoneyOff,
            text = "Penalties   Ksh${"%.2f".format(penaltyTotal)}",
            onClick = {
                onNavToPenalty()
                onClose()
            }
        )
        Spacer(modifier = Modifier.weight(1f))
        DrawerItem(
            icon = Icons.Default.MonetizationOn,
            text = "Benefits   Ksh${"%.2f".format(benefitTotal)}",
            onClick = {
                onNavToBenefit()
                onClose()
            }
        )
        Spacer(modifier = Modifier.weight(1f))
        DrawerItem(
            icon = Icons.Default.ShoppingCart,
            text = "Expenses   Ksh${"%.2f".format(expenseTotal)}",
            onClick = {
                onNavToExpense()
                onClose()
            }
        )
        Spacer(modifier = Modifier.weight(1f))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                "About Us",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ContactItem(
                text = "0795301955",
                icon = Icons.Default.Phone
            )

            ContactItem(
                text = "alexdemaish@gmail.com",
                icon = Icons.Default.Email
            )

            Row(
                modifier = Modifier.padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                SocialIcon(R.drawable.ic_github, "GitHub")
                SocialIcon(R.drawable.ic_facebook, "Facebook")
                SocialIcon(R.drawable.ic_linkedin, "LinkedIn")
            }
        }
    }
}

@Composable
fun GroupListItem(
    group: Group,
    onGroupSelected: (Group) -> Unit
) {
    Card(
        onClick = { onGroupSelected(group) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(LightAccentBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = group.name.take(1).uppercase(),
                    color = PremiumNavy,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = group.name,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ContactItem(text: String, icon: ImageVector) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp
        )
    }
}

@Composable
fun SocialIcon(iconRes: Int, description: String) {
    Icon(
        painter = painterResource(iconRes),
        contentDescription = description,
        tint = Color.White,
        modifier = Modifier
            .size(32.dp)
            .clickable { /* Handle social link click */ }
    )
}

//@Composable
//fun DrawerItem(
//    icon: ImageVector,
//    text: String,
//    onClick: () -> Unit
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable(onClick = onClick)
//            .padding(vertical = 12.dp, horizontal = 16.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Icon(
//            icon,
//            contentDescription = text,
//            tint = Color.White,
//            modifier = Modifier.size(24.dp)
//        )
//        Spacer(modifier = Modifier.width(16.dp))
//        Text(
//            text = text,
//            color = Color.White,
//            fontSize = 16.sp
//        )
//    }
//}

data class TabItem(
    val destination: NavigationDestination,
    val icon: ImageVector,
    val title: String
)