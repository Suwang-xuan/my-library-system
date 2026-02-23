@echo off
chcp 65001 >nul
echo ============================================
echo       图书馆管理系统 - 启动脚本
echo ============================================
echo.

REM 检查Java环境
echo [1/4] 检查Java环境...
java -version >nul 2>&1
if errorlevel 1 (
    echo 错误: 未检测到Java环境，请先安装JDK 8+
    pause
    exit /b 1
)
echo ✓ Java环境检测成功

REM 检查Node.js环境
echo [2/4] 检查Node.js环境...
node -v >nul 2>&1
if errorlevel 1 (
    echo 错误: 未检测到Node.js环境，请先安装Node.js 14+
    pause
    exit /b 1
)
echo ✓ Node.js环境检测成功

REM 检查数据库连接
echo [3/4] 检查数据库连接...
echo 请确保MySQL服务已启动，并且数据库library_db已创建
echo 数据库配置信息:
echo   - 数据库名: library_db
echo   - 用户名: root
echo   - 密码: 123456
echo.
set /p confirm="数据库是否已配置好? (y/n): "
if /i "%confirm%" neq "y" (
    echo 请先配置数据库，然后重新运行此脚本
    pause
    exit /b 1
)
echo ✓ 数据库连接确认

echo.
echo [4/4] 启动服务...
echo.
echo 步骤1: 启动后端服务 (端口: 8080)
echo   后端日志将在新窗口中显示
echo   请等待后端完全启动后再启动前端
echo.
set /p start_backend="是否启动后端服务? (y/n): "
if /i "%start_backend%" equ "y" (
    echo 正在启动后端服务...
    start "图书馆管理系统 - 后端服务" cmd /c "cd /d %~dp0backend && mvn spring-boot:run"
    echo 已启动后端服务，请查看新窗口中的日志
    echo 等待后端启动中...
    timeout /t 15 /nobreak >nul
    echo 后端服务应该已启动完成
)

echo.
echo 步骤2: 启动前端服务 (端口: 5173)
echo   前端将在浏览器中自动打开
echo.
set /p start_frontend="是否启动前端服务? (y/n): "
if /i "%start_frontend%" equ "y" (
    echo 正在启动前端服务...
    echo 请在新的终端窗口中执行以下命令:
    echo   cd %~dp0frontend
    echo   npm install
    echo   npm run dev
    echo.
    echo 或者直接运行 startup-frontend.bat
    echo.
    start notepad "%~dp0frontend\startup-frontend.bat"
)

echo.
echo ============================================
echo       启动完成！
echo ============================================
echo.
echo 登录信息:
echo   超级管理员: admin
echo   密码: 123456
echo.
echo 访问地址:
echo   前端: http://localhost:5173
echo   后端API: http://localhost:8080
echo.
pause