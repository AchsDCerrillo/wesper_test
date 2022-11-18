package com.codetecuhtli.wesper.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToLong

@HiltViewModel
class GameViewModel @Inject constructor(

) : ViewModel() {

    private val _counter: MutableStateFlow<Float> = MutableStateFlow(3000f)
    private val _machineSelection = MutableStateFlow(-1)
    val machineSelection = _machineSelection.asStateFlow()
    private val _userSelection = MutableStateFlow(-1)
    val userSelection = _userSelection.asStateFlow()
    private val _machineSelectedSquares =
        MutableStateFlow<MutableMap<Int, MutableList<Int>>>(mutableMapOf())
    private val machineSelectedSquares = _machineSelectedSquares.asStateFlow()
    private val _userSelectedSquares =
        MutableStateFlow<MutableMap<Int, MutableList<Int>>>(mutableMapOf())
    private val _score: MutableStateFlow<Int> = MutableStateFlow(1)
    val score = _score.asStateFlow()
    private val _turn: MutableStateFlow<Int> = MutableStateFlow(0)
    private val _isMachine: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isMachine = _isMachine.asStateFlow()
    private val _stop: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val stop = _stop.asStateFlow()
    private val _machineTurn: MutableStateFlow<Int> = MutableStateFlow(0)

    fun start() {
        viewModelScope.launch {
            if (_stop.value) _turn.update { 0 }
            _stop.update { false }
            if (_turn.value == 0) {
                _score.update { 1 }
            }
            updateTurn()
            updateStatus()
            if (!_stop.value) {
                if (!_isMachine.value) {
                    _stop.value = true
                } else {
                    _machineTurn.update { it + 1 }
                }
                for (i in (0 until _machineTurn.value)) {
                    if (_stop.value || _score.value == 0) {
                        stop()
                        break
                    }
                    val randomSquareId = randomNumber(_turn.value, 0..15)
                    _machineSelection.update {
                        randomSquareId
                    }
                    _machineSelectedSquares.update { currentMap ->
                        if (currentMap.contains(_turn.value)) currentMap[_turn.value]?.add(
                            randomSquareId
                        )
                        else currentMap[_turn.value] = mutableListOf(randomSquareId)
                        currentMap
                    }
                    delay(_counter.value.roundToLong())
                    _machineSelection.update { -1 }
                }
                updateTurn()
                updateStatus()
            }
        }
    }

    fun user(id: Int) {
        viewModelScope.launch {
            if (!_stop.value && !_isMachine.value) {
                _userSelection.update { id }
                _userSelectedSquares.update { currentMap ->
                    if (currentMap.contains(_turn.value)) currentMap[_turn.value]?.add(id)
                    else currentMap[_turn.value] = mutableListOf(id)
                    currentMap
                }
                val userSelectionSize = (_userSelectedSquares.value[_turn.value]?.size ?: 0)
                val machineSelectionSize = (_machineSelectedSquares.value[_turn.value - 1]?.size ?: 0)
                when {
                    userSelectionSize < machineSelectionSize -> {
                        val isSuccess = compareResults()
                        _score.update { if (isSuccess) it + 1 else it - 1 }
                    }
                    userSelectionSize == machineSelectionSize -> {
                        val isSuccess = compareResults()
                        _score.update { if (isSuccess) it + 1 else it - 1 }
                        delay(_counter.value.roundToLong())
                        _userSelection.update { -1 }
                        start()
                    }
                    _score.value <= 0 -> {
                        _stop.update { true }
                    }
                    else -> {
                        start()
                    }
                }
            }
        }
    }

    fun stop() = viewModelScope.launch {
        _stop.update { true }
        _machineTurn.update { 0 }
        _turn.update { 0 }
        _userSelection.update { -1 }
        _machineSelection.update { -1 }
        _machineSelectedSquares.update { mutableMapOf() }
        _userSelectedSquares.update { mutableMapOf() }
    }

    private fun updateTurn() {
        _turn.getAndUpdate {
            if (it % 5 == 0) _counter.update { value -> value * 0.9f }
            it + 1
        }
    }

    private fun updateStatus() {
        _isMachine.getAndUpdate {
            _turn.value % 2 != 0
        }
    }

    private fun compareResults(): Boolean {
        val userList = _userSelectedSquares.value[_turn.value] ?: emptyList()
        val userLastItem = userList.last()
        val userLastIndex = userList.size - 1
        val machineList = _machineSelectedSquares.value[_turn.value - 1] ?: emptyList()
        val machineValueToCompare = machineList[userLastIndex]
        return machineValueToCompare == userLastItem
    }

    private fun randomNumber(turn: Int, fromTo: IntRange): Int {
        val list = machineSelectedSquares.value[turn] ?: emptyList()
        var isSuccess = false
        var randomValue = 0
        while (!isSuccess) {
            randomValue = (fromTo).shuffled().first()
            isSuccess = !list.contains(randomValue)
        }
        return randomValue
    }

}