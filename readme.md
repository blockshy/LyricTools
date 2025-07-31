# QQ音乐QRC文件解密与歌词合并工具

1. 解密QQ音乐本地QRC歌词文件
2. 基于时间轴合并多个歌词文件

## 1. QRC文件解密
**针对QQ音乐QRC格式的已验证解决方案**  
经测试发现，本地QRC文件无法直接通过其他开源项目的常规方法解密。本实现通过对文件字节流进行特定处理，成功实现了解密功能。

**实现位置：**  
解密逻辑详见 `utils.QrcLyricDecoder.decodeByQrcFile`

## 2. 歌词文件合并
**基于时间轴的歌词合并**  
支持将多个歌词文件按照时间轴顺序合并，保留时间戳信息。当前支持格式：
- LRC格式 (`.lrc`)
- SRT格式 (`.srt`)

## 运行环境
- **需要JDK 21**  
  运行前请确保已安装Java开发工具包版本21

-----------------------------------------

# QQ Music QRC File Decryption and Lyric Merger

1. Decrypt QQ Music's local QRC lyric files
2. Merge multiple lyric files based on their timelines

## 1. QRC File Decryption
**Verified solution for QQ Music's QRC format**  
Local QRC files cannot be decrypted using standard methods from other open-source projects. This implementation processes the file byte stream with specific transformations to successfully decrypt the content.

**Implementation:**  
See `utils.QrcLyricDecoder.decodeByQrcFile` for the decryption logic.

## 2. Lyric File Merger
**Timeline-based lyric merging**  
Merges content from multiple lyric files while preserving chronological order. Currently supports these formats:
- LRC (`.lrc`)
- SRT (`.srt`)

## Runtime Environment
- **Requires JDK 21**  
  Ensure you have Java Development Kit version 21 installed before running the project.
