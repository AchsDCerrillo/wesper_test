package com.codetecuhtli.wesper.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import com.codetecuhtli.wesper.R
import com.codetecuhtli.wesper.game.GameViewModel
import com.codetecuhtli.wesper.ui.theme.Dimens
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun Timer(modifier: Modifier = Modifier, isRunning: Boolean) {
    var currentTime by remember {
        mutableStateOf(0L)
    }
    LaunchedEffect(key1 = currentTime, key2 = isRunning) {
        if(currentTime >= 0 && isRunning) {
            delay(1000L)
            currentTime++
        }
    }
    Text(
        modifier = modifier,
        text = stringResource(R.string.time, TimeUnit.SECONDS.toHours(currentTime) % 24, TimeUnit.SECONDS.toMinutes(currentTime) % 60, TimeUnit.SECONDS.toSeconds(currentTime % 60)),
        textAlign = TextAlign.Center,
    )
}

@Composable
fun ItemSquare(
    modifier: Modifier = Modifier,
    id: Int,
    isSelected: Boolean,
    selectedColor: Color,
    onSelected: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .defaultMinSize(minHeight = 50.dp, minWidth = 50.dp)
            .clip(RectangleShape)
            .background(if (isSelected) selectedColor else Color.Blue)
            .clickable { onSelected(id) }
            .aspectRatio(1f)
            .then(modifier)
    )
}

@Composable
fun Board(
    modifier: Modifier = Modifier,
    selectedId: Int = -1,
    selectedColor: Color,
    blocked: Boolean = false,
    columns: Int = 4,
    rows: Int = 4,
    onSelected: (Int) -> Unit
) {
    LazyVerticalGrid(
        modifier = Modifier
            .padding(Dimens.keyLine2)
            .then(modifier),
        columns = GridCells.Fixed(columns),
        verticalArrangement = Arrangement.spacedBy(Dimens.keyLineQuarter),
        horizontalArrangement = Arrangement.spacedBy(Dimens.keyLineQuarter)
    ) {
        items(columns * rows) { item ->
            ItemSquare(
                id = item,
                isSelected = item == selectedId,
                selectedColor = selectedColor,
                onSelected = { id ->
                    if (!blocked) onSelected(id)
                }
            )
        }
    }
}

@Composable
fun GameScreen(
    viewModel: GameViewModel = hiltViewModel()
) {
    val machineSelection by viewModel.machineSelection.collectAsState()
    val userSelection by viewModel.userSelection.collectAsState()
    val score by viewModel.score.collectAsState()
    val stop by viewModel.stop.collectAsState()
    val isMachineTurn by viewModel.isMachine.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxSize()
        ) {
            val (boardRef, scoreRef, timerRef, startButtonRef, infoRef) = createRefs()
            Board(
                modifier = Modifier.constrainAs(boardRef) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
                selectedId = if (isMachineTurn) machineSelection else userSelection,
                selectedColor = if (isMachineTurn) Color.White else Color.Cyan,
                blocked = score == 0 || isMachineTurn || stop,
                onSelected = viewModel::user
            )
            Text(
                modifier = Modifier.constrainAs(scoreRef) {
                    top.linkTo(boardRef.bottom)
                    start.linkTo(parent.start)
                    width = Dimension.fillToConstraints
                },
                text = stringResource(id = R.string.score, score),
                textAlign = TextAlign.Center,
            )
            Timer(
                modifier = Modifier.constrainAs(timerRef) {
                    top.linkTo(boardRef.bottom)
                    start.linkTo(scoreRef.end)
                    width = Dimension.fillToConstraints
                },
                isRunning = true
            )
            Button(
                modifier = Modifier.constrainAs(startButtonRef) {
                    bottom.linkTo(parent.bottom, Dimens.keyLine2)
                    start.linkTo(parent.start, Dimens.keyLine2)
                    end.linkTo(parent.end, Dimens.keyLine2)
                    width = Dimension.fillToConstraints
                },
                onClick = { if (stop) viewModel.start() else viewModel.stop() }
            ) {
                Text(text = if (stop) stringResource(R.string.start) else stringResource(R.string.stop))
            }
            if (!stop && score > 0) {
                Text(
                    modifier = Modifier.constrainAs(infoRef) {
                        bottom.linkTo(startButtonRef.top)
                        top.linkTo(timerRef.bottom)
                        start.linkTo(parent.start, Dimens.keyLine2)
                        end.linkTo(parent.end, Dimens.keyLine2)
                        width = Dimension.fillToConstraints
                    },
                    text = if (isMachineTurn) stringResource(R.string.machine_turn) else stringResource(
                                            R.string.human_turn),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.h6
                )
            }
            if (score == 0) {
                Text(
                    modifier = Modifier.constrainAs(infoRef) {
                        bottom.linkTo(startButtonRef.top)
                        top.linkTo(timerRef.bottom)
                        start.linkTo(parent.start, Dimens.keyLine2)
                        end.linkTo(parent.end, Dimens.keyLine2)
                        width = Dimension.fillToConstraints
                    },
                    text = stringResource(R.string.game_over),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.h3
                )
            }
            createHorizontalChain(
                scoreRef, timerRef,
                chainStyle = ChainStyle.SpreadInside
            )
        }
    }
}