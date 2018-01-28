/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package gui;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import monitor.MonitorInfo;
import monitor.VirtualScreenBoundingBox;

class RectangleResizeHelper implements EventHandler<MouseEvent> {

    private Stage stage;
    private Cursor cursor = Cursor.MOVE;
    private int topLeftX;
    private int topLeftY;
    private int lowerRightX;
    private int lowerRightY;

    private double moveOffsetX;
    private double moveOffsetY;

    RectangleResizeHelper() {
    }

    void setStageToHandle(Stage stage) {
        this.stage = stage;
        VirtualScreenBoundingBox boundingBox = MonitorInfo.getVirtualScreenBoundingBox();
        topLeftX = boundingBox.topLeftX;
        topLeftY = boundingBox.topLeftY;
        lowerRightX = boundingBox.lowerRightX;
        lowerRightY = boundingBox.lowerRightY;
        stage.addEventHandler(MouseEvent.MOUSE_MOVED, this);
        stage.addEventHandler(MouseEvent.MOUSE_PRESSED, this);
        stage.addEventHandler(MouseEvent.MOUSE_DRAGGED, this);
        stage.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyEvent);
    }

    @Override
    public void handle(MouseEvent event) {
        Scene scene = stage.getScene();
        final int resizeSpaceFromEdge = 30;
        final int minWidthHeight = 200;
        EventType<? extends MouseEvent> eventType = event.getEventType();

        if (eventType.equals(MouseEvent.MOUSE_MOVED)) {
            double sceneMouseX = event.getSceneX();
            double sceneMouseY = event.getSceneY();
            double sceneWidth = scene.getWidth();
            double sceneHeight = scene.getHeight();

            if (sceneMouseX < resizeSpaceFromEdge && sceneMouseY < resizeSpaceFromEdge) {
                cursor = Cursor.NW_RESIZE;
            } else if (sceneMouseX < resizeSpaceFromEdge && sceneMouseY > sceneHeight-resizeSpaceFromEdge) {
                cursor = Cursor.SW_RESIZE;
            } else if (sceneMouseX > sceneWidth-resizeSpaceFromEdge && sceneMouseY < resizeSpaceFromEdge) {
                cursor = Cursor.NE_RESIZE;
            } else if (sceneMouseX > sceneWidth-resizeSpaceFromEdge && sceneMouseY > sceneHeight-resizeSpaceFromEdge) {
                cursor = Cursor.SE_RESIZE;
            } else if (sceneMouseX < resizeSpaceFromEdge) {
                cursor = Cursor.W_RESIZE;
            } else if (sceneMouseX > sceneWidth-resizeSpaceFromEdge) {
                cursor = Cursor.E_RESIZE;
            } else if (sceneMouseY < resizeSpaceFromEdge) {
                cursor = Cursor.N_RESIZE;
            } else if (sceneMouseY > sceneHeight-resizeSpaceFromEdge) {
                cursor = Cursor.S_RESIZE;
            } else {
                cursor = Cursor.MOVE;
            }
            scene.setCursor(cursor);
        } else if (eventType.equals(MouseEvent.MOUSE_PRESSED)) {
            if (cursor.equals(Cursor.MOVE)) {
                moveOffsetX = Math.abs(event.getSceneX());
                moveOffsetY = Math.abs(event.getSceneY());
            }
        } else if (eventType.equals(MouseEvent.MOUSE_DRAGGED)) {
            if (cursor.equals(Cursor.MOVE)) {
                double mouseX = event.getScreenX();
                double mouseY = event.getScreenY();
                double newWinX = mouseX - moveOffsetX;
                double newWinY = mouseY - moveOffsetY;
                double stageWidth = stage.getWidth();
                double stageHeight = stage.getHeight();
                boolean outsideLeft = newWinX < topLeftX;
                boolean outsideRight = (newWinX+stageWidth) > lowerRightX;
                boolean notOutsideLeftAndRight = !outsideLeft && !outsideRight;
                boolean outsideTop = newWinY < topLeftY;
                boolean outsideBottom = (newWinY+stageHeight) > lowerRightY;
                boolean notOutsideBottomAndTop = !outsideTop && !outsideBottom;
                if (notOutsideLeftAndRight) {
                    stage.setX(newWinX);
                } else {
                    moveOffsetX = Math.abs(event.getSceneX());
                    if (outsideLeft) {
                        stage.setX(topLeftX);
                    } else {
                        stage.setX(lowerRightX-stageWidth);
                    }
                }
                if (notOutsideBottomAndTop) {
                    stage.setY(newWinY);
                } else {
                    moveOffsetY = Math.abs(event.getSceneY());
                    if (outsideTop) {
                        stage.setY(topLeftY);
                    } else {
                        stage.setY(lowerRightY-stageHeight);
                    }
                }
            } else if (cursor.equals(Cursor.NW_RESIZE)) {
                double mouseX = event.getScreenX();
                double winX = stage.getX();
                double stageWidth = stage.getWidth();
                double newWinWidth = (winX-mouseX) + stageWidth;

                double mouseY = event.getScreenY();
                double winY = stage.getY();
                double stageHeight = stage.getHeight();
                double newWinHeight = (winY-mouseY) + stageHeight;

                if (newWinWidth >= minWidthHeight) {
                    stage.setX(mouseX);
                    stage.setWidth(newWinWidth);
                } else {
                    stage.setX((winX+stageWidth) - minWidthHeight);
                    stage.setWidth(minWidthHeight);
                }
                if (newWinHeight >= minWidthHeight) {
                    stage.setY(mouseY);
                    stage.setHeight(newWinHeight);
                } else {
                    stage.setY((winY+stageHeight) - minWidthHeight);
                    stage.setHeight(minWidthHeight);
                }
            } else if (cursor.equals(Cursor.SW_RESIZE)) {
                double mouseX = event.getScreenX();
                double winX = stage.getX();
                double stageWidth = stage.getWidth();
                double newWinWidth = (winX-mouseX) + stageWidth;

                double mouseY = event.getScreenY();
                double winY = stage.getY();
                // not to self: +1 because coords start at 0, not needed with oldWin<Width/Height> because that's
                // already calculated with +1 before, so only need the difference
                double newWinHeight = (mouseY-winY) + 1;

                if (newWinWidth >= minWidthHeight) {
                    stage.setX(mouseX);
                    stage.setWidth(newWinWidth);
                } else {
                    stage.setX((winX+stageWidth) - minWidthHeight);
                    stage.setWidth(minWidthHeight);
                }
                if (newWinHeight >= minWidthHeight) {
                    stage.setHeight(newWinHeight);
                } else {
                    stage.setHeight(minWidthHeight);
                }
            } else if (cursor.equals(Cursor.NE_RESIZE)) {
                double mouseX = event.getScreenX();
                double winX = stage.getX();
                double newWinWidth = (mouseX-winX) + 1;

                double mouseY = event.getScreenY();
                double winY = stage.getY();
                double stageHeight = stage.getHeight();
                double newWinHeight = winY - mouseY + stageHeight;

                if (newWinWidth >= minWidthHeight) {
                    stage.setWidth(newWinWidth);
                } else {
                    stage.setWidth(minWidthHeight);
                }
                if (newWinHeight >= minWidthHeight) {
                    stage.setY(mouseY);
                    stage.setHeight(newWinHeight);
                } else {
                    stage.setY((winY+stageHeight) - minWidthHeight);
                    stage.setHeight(minWidthHeight);
                }
            } else if (cursor.equals(Cursor.SE_RESIZE)) {
                double mouseX = event.getScreenX();
                double mouseY = event.getScreenY();
                double winX = stage.getX();
                double winY = stage.getY();
                double newWinWidth = (mouseX-winX) + 1;
                double newWinHeight = (mouseY-winY) + 1;
                if (newWinWidth >= minWidthHeight) {
                    stage.setWidth(newWinWidth);
                } else {
                    stage.setWidth(minWidthHeight);
                }
                if (newWinHeight >= minWidthHeight) {
                    stage.setHeight(newWinHeight);
                } else {
                    stage.setHeight(minWidthHeight);
                }
            } else if (cursor.equals(Cursor.W_RESIZE)) {
                double mouseX = event.getScreenX();
                double winX = stage.getX();
                double stageWidth = stage.getWidth();
                double newWinWidth = winX - mouseX + stageWidth;
                if (newWinWidth >= minWidthHeight) {
                    stage.setX(mouseX);
                    stage.setWidth(newWinWidth);
                } else {
                    stage.setX((winX+stageWidth) - minWidthHeight);
                    stage.setWidth(minWidthHeight);
                }
            } else if (cursor.equals(Cursor.E_RESIZE)) {
                double mouseX = event.getScreenX();
                double winX = stage.getX();
                double newWinWidth = (mouseX-winX) + 1;
                if (newWinWidth >= minWidthHeight) {
                    stage.setWidth(newWinWidth);
                } else {
                    stage.setWidth(minWidthHeight);
                }
            } else if (cursor.equals(Cursor.N_RESIZE)) {
                double mouseY = event.getScreenY();
                double winY = stage.getY();
                double stageHeight = stage.getHeight();
                double newWinHeight = winY - mouseY + stageHeight;
                if (newWinHeight >= minWidthHeight) {
                    stage.setY(mouseY);
                    stage.setHeight(newWinHeight);
                } else {
                    stage.setY((winY+stageHeight) - minWidthHeight);
                    stage.setHeight(minWidthHeight);
                }
            } else if (cursor.equals(Cursor.S_RESIZE)) {
                double mouseY = event.getScreenY();
                double winY = stage.getY();
                double newWinHeight = (mouseY-winY) + 1;
                if (newWinHeight >= minWidthHeight) {
                    stage.setHeight(newWinHeight);
                } else {
                    stage.setHeight(minWidthHeight);
                }
            }
        }
    }

    private void handleKeyEvent(KeyEvent event) {
        double winX = stage.getX();
        double winY = stage.getY();
        double newWinX, newWinY;
        switch (event.getCode()) {
            case LEFT:
                newWinX = winX - 1;
                if (newWinX >= topLeftX) {
                    stage.setX(newWinX);
                }
                break;
            case DOWN:
                newWinY = winY + 1;
                boolean notOutsideBottom = newWinY + stage.getHeight() <= lowerRightY;
                if (notOutsideBottom) {
                    stage.setY(newWinY);
                }
                break;
            case RIGHT:
                newWinX = winX + 1;
                boolean notOutsideRight = newWinX + stage.getWidth() <= lowerRightX;
                if (notOutsideRight) {
                    stage.setX(newWinX);
                }
                break;
            case UP:
                newWinY = winY - 1;
                if (newWinY >= topLeftY) {
                    stage.setY(newWinY);
                }
                break;
            case ESCAPE:
                stage.close();
                break;
        }
    }

}
