; Merlin NSIS Installer
; Copyright 2004 The Apache Software Foundation
;
; Licensed under the Apache License, Version 2.0 (the "License");
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;    http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.

!define ALL_USERS

!define TEMP $R0

!include "WriteEnvStr.nsh"
!include "PathMunge.nsh"
!include "MUI.nsh"

;--------------------------------
;Configuration

  ;General
  Name "Merlin Platform @VERSION@"
  OutFile "merlin-@VERSION@-install.exe"

  ;Folder selection page
  InstallDir "C:\merlin"
  
  ;Remember install folder
  InstallDirRegKey HKCU "Software\Merlin Platform 3.3" ""

;--------------------------------
;Variables

  Var MUI_TEMP
  Var STARTMENU_FOLDER

;--------------------------------
;Interface Settings

  !define MUI_HEADERIMAGE
  !define MUI_ABORTWARNING

;--------------------------------
;Pages

  !insertmacro MUI_PAGE_WELCOME
  !insertmacro MUI_PAGE_LICENSE "LICENSE.TXT"
  !insertmacro MUI_PAGE_COMPONENTS
  !insertmacro MUI_PAGE_DIRECTORY

  ;Start Menu Folder Page Configuration
  !define MUI_STARTMENUPAGE_REGISTRY_ROOT "HKCU"
  !define MUI_STARTMENUPAGE_REGISTRY_KEY "Software\Modern UI Test"
  !define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "Start Menu Folder"

  !insertmacro MUI_PAGE_STARTMENU Application $STARTMENU_FOLDER

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
;Language Strings

  ;Description
  LangString DESC_SecMerlin   ${LANG_ENGLISH} "Installs Base Merlin Platform"
  LangString DESC_SecService  ${LANG_ENGLISH} "Installs Merlin NT Service"

; LangString DESC_SecFacilities  ${LANG_ENGLISH} "Installs Maven Facilities"  

;--------------------------------
;Installer Sections

Section "merlin base" SecMerlin

  SetOutPath $INSTDIR\bin
  File /r target\deliverables\bin\*
  SetOutPath $INSTDIR\config
  File /r target\deliverables\config\*
  SetOutPath $INSTDIR\system
  File /r target\deliverables\lib\*
  SetOutPath $INSTDIR\system
  File /r target\merlin\system\*
  SetOutPath $INSTDIR
  File  target\deliverables\NOTICE.TXT
  File  target\deliverables\LICENSE.TXT
  File  target\deliverables\INSTALL.TXT

  Push "MERLIN_HOME"
  Push $INSTDIR
  Call WriteEnvStr
  
  Push $INSTDIR\bin
  Call AddToPath

  ;Store install folder
  WriteRegStr HKCU "Software\Merlin Platform 3.3" "" $INSTDIR

  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application

    ;Create shortcuts
    CreateDirectory "$SMPROGRAMS\$STARTMENU_FOLDER"
    CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\Uninstall.lnk" "$INSTDIR\Uninstall.exe"

  !insertmacro MUI_STARTMENU_WRITE_END
  
  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"

SectionEnd

Section "merlin service" SecService
  Exec "$INSTDIR\bin\nt\Wrapper.exe -i $INSTDIR\bin\nt\wrapper.conf"
SectionEND

;--------------------------------
;Descriptions

!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${SecMerlin} $(DESC_SecMerlin)
  !insertmacro MUI_DESCRIPTION_TEXT ${SecDoc} $(DESC_SecDoc)
  !insertmacro MUI_DESCRIPTION_TEXT ${SecTutorial} $(DESC_SecTutorial)
  !insertmacro MUI_DESCRIPTION_TEXT ${SecPlugins} $(DESC_SecPlugins)
  !insertmacro MUI_DESCRIPTION_TEXT ${SecService} $(DESC_SecService)
!insertmacro MUI_FUNCTION_DESCRIPTION_END
 
;--------------------------------
;Uninstaller Section

Section "Uninstall"

  Exec "$INSTDIR\bin\nt\Wrapper.exe -r $INSTDIR\bin\nt\wrapper.conf"

  Push "MERLIN_HOME"
  Call un.DeleteEnvStr

  Push $INSTDIR\bin
  Call un.RemoveFromPath

  !insertmacro MUI_STARTMENU_GETFOLDER Application $MUI_TEMP

  Delete "$SMPROGRAMS\$MUI_TEMP\Uninstall.lnk"
  RMDir /r "$SMPROGRAMS\$MUI_TEMP"

  RMDir /r $INSTDIR
  
SectionEnd

; todo:
;    Check for JVM and Version.  Install endorsed jars if needed
;      see  http://nsis.sourceforge.net/archive/nsisweb.php?page=543&instances=0
;    Check for if we are installing for one user or many
;    Install a default app that includes the facilities

; eof
