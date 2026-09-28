package com.threew.tv.player;

public class PlayerSeekController {

    private final PlayerPositionCalculator calculator;

    public PlayerSeekController() {
        calculator = new PlayerPositionCalculator();
    }

    public PlayerSeekResult seekAbsolute(long currentPositionMs,
                                         long targetPositionMs,
                                         long durationMs) {
        long oldPosition = calculator.clamp(
                currentPositionMs,
                durationMs
        );

        long newPosition = calculator.clamp(
                targetPositionMs,
                durationMs
        );

        return new PlayerSeekResult(
                oldPosition,
                newPosition,
                durationMs
        );
    }

    public PlayerSeekResult seekRelative(long currentPositionMs,
                                         long deltaMs,
                                         long durationMs) {
        long oldPosition = calculator.clamp(
                currentPositionMs,
                durationMs
        );

        long newPosition;

        if (deltaMs >= 0L) {
            newPosition = calculator.add(
                    oldPosition,
                    deltaMs,
                    durationMs
            );
        } else {
            newPosition = calculator.subtract(
                    oldPosition,
                    -deltaMs
            );
        }

        return new PlayerSeekResult(
                oldPosition,
                newPosition,
                durationMs
        );
    }

    public long calculateResumePosition(long savedPositionMs,
                                        long durationMs) {
        return calculator.calculateResumePosition(
                savedPositionMs,
                durationMs
        );
    }
}