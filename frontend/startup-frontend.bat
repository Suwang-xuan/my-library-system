@echo off
chcp 65001 >nul
echo ============================================
echo    图书馆管理系统 - 前端启动脚本
echo ============================================
echo.

REM 进入前端目录
cd /d "%~dp0frontend"

REM 检查是否已安装依赖
if not exist "node_modules" (
    echo 正在安装前端依赖...
    echo 这可能需要几分钟时间，请耐心等待...
    npm install
    if errorlevel 1 (
        echo 错误: 依赖安装失败
        pause
        exit /b 1
    )
    echo ✓ 依赖安装完成
) else (
    echo ✓ 依赖已安装
)

echo.
echo 正在启动前端开发服务器...
echo 前端服务启动后将在浏览器中自动打开
echo 访问地址: http://localhost:5173
echo.
echo 按 Ctrl+C 可停止服务
echo.

npm run dev

pause