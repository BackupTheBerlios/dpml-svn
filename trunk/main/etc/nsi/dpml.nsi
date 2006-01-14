; DPML NSIS Installer
; Copyright 2004 Stephen McConnell
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
!define VERSION '@PROJECT-VERSION@'

!include "WriteEnvStr.nsh"
!include "PathManipulation.nsh"
!include "MUI.nsh"

;--------------------------------
;Configuration

  ;General
  Name "DPML SDK"
  Caption "DPML SDK (${VERSION}) Setup"
  OutFile "dpml-sdk-win32-@PROJECT-VERSION@.exe"

  ;Folder selection page
  InstallDir $PROGRAMFILES\DPML
 
  ;Remember install folder
  InstallDirRegKey HKLM "Software\DPML" ""

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
  !insertmacro MUI_PAGE_LICENSE "..\bundle\LICENSE.txt"
  !insertmacro MUI_PAGE_COMPONENTS
  !insertmacro MUI_PAGE_DIRECTORY

  ;Start Menu Folder Page Configuration
  !define MUI_STARTMENUPAGE_REGISTRY_ROOT "HKCU"
  !define MUI_STARTMENUPAGE_REGISTRY_KEY "Software\DPML"
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
  LangString DESC_SecData  ${LANG_ENGLISH} "Installs Local data"
  LangString DESC_SecPrefs  ${LANG_ENGLISH} "Installs Default Preferences"
  LangString DESC_SecShare  ${LANG_ENGLISH} "Installs Shared Libraries"
  LangString DESC_SecPlatform ${LANG_ENGLISH} "Installs Transit Resource Management, Depot Build Management, Metro Runtime and the Station Application Controller"
  LangString DESC_SecStation  ${LANG_ENGLISH} "Installs Station SCM"
  LangString DESC_SecDoc      ${LANG_ENGLISH} "Installs Platform and API Documentation"

;--------------------------------
;Installer Sections

Section "-platform" SecPlatform

  SetOutPath $INSTDIR
  File  ..\bundle\README.txt
  File  ..\bundle\LICENSE.txt
  File  ..\bundle\NOTICE.txt
  
  Push "DPML_HOME"
  Push $INSTDIR
  Call WriteEnvStr
  
  Push $INSTDIR\share\bin
  Call AddToPath

  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
    ;Create shortcuts
    CreateDirectory "$SMPROGRAMS\$STARTMENU_FOLDER"
    CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\Uninstall.lnk" "$INSTDIR\Uninstall.exe"
  !insertmacro MUI_STARTMENU_WRITE_END
  
  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"

SectionEnd

Section "data" SecData
  SectionIn RO
  SetOutPath $INSTDIR\data
  File /nonfatal /r  ..\bundle\data\*
SectionEND

Section "prefs" SecPrefs
  SectionIn RO
  SetOutPath $INSTDIR\prefs
  File /r ..\bundle\prefs\*
SectionEND

Section "share" SecShare
  SectionIn RO
  SetOutPath $INSTDIR\share\bin
  File ..\bundle\share\bin\security.policy
  File ..\bundle\share\bin\transit.exe
  File ..\bundle\share\bin\transit.lap
  File ..\bundle\share\bin\depot.exe
  File ..\bundle\share\bin\depot.lap
  File ..\bundle\share\bin\build.exe
  File ..\bundle\share\bin\build.lap
  File ..\bundle\share\bin\metro.exe
  File ..\bundle\share\bin\metro.lap
  File ..\bundle\share\bin\station.exe
  File ..\bundle\share\bin\station.lap
  SetOutPath $INSTDIR\share\bin\scm
  File /r ..\bundle\share\bin\scm\*
  SetOutPath $INSTDIR\share\lib
  File /r ..\bundle\share\lib\*
  SetOutPath $INSTDIR\share\local
  File /r ..\bundle\share\local\*
SectionEND

Section "docs" SecDoc
  SetOutPath $INSTDIR\share\docs
  File /r ..\bundle\share\docs\*
  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
    CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\DPML Home (local).lnk" "$INSTDIR\share\docs\index.html"
    CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\DPML Platform API.lnk" "$INSTDIR\share\docs\api\dpml\@PROJECT-VERSION@\index.html"
  !insertmacro MUI_STARTMENU_WRITE_END
SectionEND

Section "station scm" SecStation
  Exec "$INSTDIR\share\bin\scm\wrapper.exe -i $INSTDIR\share\bin\scm\conf\wrapper.conf"
SectionEND

Section "-post"
  WriteRegStr HKLM "Software\DPML" "" $INSTDIR
SectionEND

;--------------------------------
;Descriptions

!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${SecPlatform} $(DESC_SecPlatform)
  !insertmacro MUI_DESCRIPTION_TEXT ${SecStation} $(DESC_SecStation)
!insertmacro MUI_FUNCTION_DESCRIPTION_END
 
;--------------------------------
;Uninstaller Section

Section "Uninstall"

  Exec "$INSTDIR\share\bin\scm\Wrapper.exe -r $INSTDIR\share\bin\scm\wrapper.conf"

  Push "DPML_HOME"
  Call un.DeleteEnvStr

  Push $INSTDIR\share\bin
  Call un.RemoveFromPath

  !insertmacro MUI_STARTMENU_GETFOLDER Application $MUI_TEMP

  Delete "$SMPROGRAMS\$MUI_TEMP\Uninstall.lnk"
  RMDir /r "$SMPROGRAMS\$MUI_TEMP"
  RMDir /r $INSTDIR
  
SectionEnd
