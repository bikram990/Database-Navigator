package com.dci.intellij.dbn.debugger.common.breakpoint;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.impl.breakpoints.XBreakpointManagerImpl;
import com.intellij.xdebugger.impl.breakpoints.XLineBreakpointImpl;
import com.intellij.xdebugger.impl.breakpoints.XLineBreakpointManager;
import static com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointUtil.getVirtualFile;
import static com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointUtil.setBreakpointId;

/**
 * WORKAROUND: Breakpoints do not seem to be registered properly in the XLineBreakpointManager.
 * This way the breakpoints get updated as soon as the file is opened.
 */
public class DBBreakpointUpdaterFileEditorListener implements FileEditorManagerListener{
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        if (file instanceof DBEditableObjectVirtualFile) {
            DBEditableObjectVirtualFile databaseFile = (DBEditableObjectVirtualFile) file;
            XBreakpointManagerImpl breakpointManager = (XBreakpointManagerImpl) XDebuggerManager.getInstance(source.getProject()).getBreakpointManager();
            for (XBreakpoint breakpoint : breakpointManager.getAllBreakpoints()) {
                if (breakpoint instanceof XLineBreakpoint) {
                    XLineBreakpoint lineBreakpoint = (XLineBreakpoint) breakpoint;
                    setBreakpointId(lineBreakpoint, null);
                    VirtualFile virtualFile = getVirtualFile(lineBreakpoint);
                    if (databaseFile.equals(virtualFile)) {
                        XLineBreakpointManager lineBreakpointManager = breakpointManager.getLineBreakpointManager();
                        lineBreakpointManager.registerBreakpoint((XLineBreakpointImpl) lineBreakpoint, true);
                    }
                }
            }
        }
    }

    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
    }

    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
    }
}