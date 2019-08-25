package com.example.gameworkflow

import com.example.gameworkflow.RealGameWorkflow.State
import com.squareup.workflow.*

class RealGameWorkflow : GameWorkflow,
    StatefulWorkflow<GameProps, State, GameEnded, GameRendering>() {

    data class State(
        val playerPosition: Point,
        val gameOver: Boolean
    )

    override fun initialState(props: GameProps, snapshot: Snapshot?): State =
        State(playerPosition = props.spawnPosition, gameOver = false)

    private fun doMove(
        props: GameProps,
        direction: Direction
    ) = WorkflowAction<State, GameEnded> {
        val newPosition = state.playerPosition.moved(
            direction = direction,
            bounds = props.boardSize
        )
        state = state.copy(
            playerPosition = newPosition,
            gameOver = newPosition == props.goalPosition
        )

        // Value returned from a WorkflowAction is emitted as workflow output.
        return@WorkflowAction if (state.gameOver) GameEnded else null
    }

    override fun render(
        props: GameProps,
        state: State,
        context: RenderContext<State, GameEnded>
    ): GameRendering {
        val sink: Sink<WorkflowAction<State, GameEnded>> = context.makeActionSink()
        val doMove = { direction: Direction -> sink.send(doMove(props, direction)) }

        return GameRendering(
            boardSize = props.boardSize,
            playerPosition = state.playerPosition,
            goalPosition = props.goalPosition,
            onMove = if (state.gameOver) null else doMove
        )
    }

    override fun snapshotState(state: State): Snapshot = Snapshot.EMPTY
}
