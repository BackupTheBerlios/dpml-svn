;NSIS Modern User Interface version 1.63
;Welcome/Finish Page Example Script
;Written by Stephen McConnell

;--------------------------------
;Include Modern UI

  !include "MUI.nsh"

;--------------------------------
;Include Environment Handler

  !include path.nsh

;--------------------------------
;Configuration

  ;General
  Name "Merlin Service Management"

  ;General
  OutFile "..\..\..\..\target\merlin-install-3.3-SNAPSHOT.exe"

  ;Folder selection page
  InstallDir "$PROGRAMFILES\Merlin"
  
  ;Remember install folder
  InstallDirRegKey HKCU "Software\Merlin" ""

;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING

;--------------------------------
;Pages

  !insertmacro MUI_PAGE_WELCOME
  !insertmacro MUI_PAGE_LICENSE "./../etc/license.txt"
  !insertmacro MUI_PAGE_COMPONENTS
  !insertmacro MUI_PAGE_DIRECTORY
  !insertmacro MUI_PAGE_INSTFILES
  !insertmacro MUI_PAGE_FINISH
  
  !insertmacro MUI_UNPAGE_WELCOME
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES
  !insertmacro MUI_UNPAGE_FINISH
  
;--------------------------------
;Languages
 
  !insertmacro MUI_LANGUAGE "English"

;--------------------------------
;Reserve Files

  ;Things that need to be extracted on first (keep these lines before any File command!)
  ;Only useful for BZIP2 compression
  ;!insertmacro MUI_RESERVEFILE_WELCOMEFINISHPAGE

;--------------------------------
Section ""

  Push "MERLIN_HOME"
  CreateDirectory $INSTDIR
  GetFullPathName /SHORT $R1 $INSTDIR
  Push $R1
  Call WriteEnvStr

  Push "%MERLIN_HOME%\bin"
  Call AddToPath

SectionEnd


;--------------------------------
;Installer Sections

Section "Merlin Kernel" SecCopyUI

  SectionIn RO

  ;Add your stuff here

  SetOutPath "$INSTDIR"
  File /r "..\..\..\..\target\merlin\**"

  ;Store install folder
  WriteRegStr HKCU "Software\Merlin" "" $INSTDIR
    
  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"

SectionEnd

Section ""

  CreateDirectory "$SMPROGRAMS\Merlin"
  CreateShortCut "$SMPROGRAMS\Merlin\Uninstall.lnk" "$INSTDIR\uninstall.exe"
  CreateShortCut "$SMPROGRAMS\Merlin\Merlin Documentation.lnk" "$INSTDIR\docs\index.html"
  CreateShortCut "$SMPROGRAMS\Merlin\Tutorials.lnk" "$INSTDIR\docs\starting\tutorial\index.html"
  CreateShortCut "$SMPROGRAMS\Merlin\System Properties.lnk" "$INSTDIR\docs\merlin\kernel\properties.html"
  CreateShortCut "$SMPROGRAMS\Merlin\XML Descriptors.lnk" "$INSTDIR\docs\meta\index.html"
  CreateShortCut "$SMPROGRAMS\Merlin\Tools.lnk" "$INSTDIR\docs\tools\index.html"

SectionEnd

;--------------------------------
;Uninstaller

Section "Uninstall"

  Push "%MERLIN_HOME%\bin"
  Call un.RemoveFromPath

  Push "MERLIN_HOME"
  Call un.DeleteEnvStr

  Delete "$INSTDIR\Uninstall.exe"
  Delete "$INSTDIR\INSTALL.TXT"
  Delete "$INSTDIR\LICENSE.TXT"
  Delete "$INSTDIR\README.TXT"

  RMDir /r "$INSTDIR\bin"
  RMDir /r "$INSTDIR\config"
  RMDir /r "$INSTDIR\system"
  RMDir "$INSTDIR"

  ; remove shortcuts, if any
  Delete "$SMPROGRAMS\Merlin\*.*"
  RMDir "$SMPROGRAMS\Merlin"

  DeleteRegKey /ifempty HKCU "Software\Merlin"

SectionEnd