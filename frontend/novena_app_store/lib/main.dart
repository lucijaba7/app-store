import 'package:flutter/material.dart';
import 'dart:io';
import 'package:dio/dio.dart';
import 'package:open_file/open_file.dart';
import 'package:path_provider/path_provider.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        // This is the theme of your application.
        //
        // TRY THIS: Try running your application with "flutter run". You'll see
        // the application has a purple toolbar. Then, without quitting the app,
        // try changing the seedColor in the colorScheme below to Colors.green
        // and then invoke "hot reload" (save your changes or press the "hot
        // reload" button in a Flutter-supported IDE, or press "r" if you used
        // the command line to start the app).
        //
        // Notice that the counter didn't reset back to zero; the application
        // state is not lost during the reload. To reset the state, use hot
        // restart instead.
        //
        // This works for code too, not just values: Most code changes can be
        // tested with just a hot reload.
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue),
        useMaterial3: true,
      ),
      home: Scaffold(
        body: Center(
          child: ElevatedButton(
            onPressed: () {
              downloadAndInstallApk('Test2').then((_) => print('downlaoded'));
            },
            child: Text("Download apk"),
          ),
        ),
      ),
    );
  }
}

Future<void> downloadAndInstallApk(String appName) async {
  try {
    String url =
        "http://localhost:3000/download/$appName"; // Change to your server URL
    Dio dio = Dio();

    // Get the app's download directory
    Directory tempDir = await getTemporaryDirectory();
    String filePath = "${tempDir.path}/$appName.apk";

    // Download the APK file
    await dio.download(url, filePath);

    // Open the APK file to install
    OpenFile.open(filePath);
  } catch (e) {
    print("Error downloading APK: $e");
  }
}
