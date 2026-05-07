import AsyncStorage from '@react-native-async-storage/async-storage';

export async function readJson<T>(key: string, fallback: T): Promise<T> {
  try {
    const raw = await AsyncStorage.getItem(key);
    return raw ? (JSON.parse(raw) as T) : fallback;
  } catch {
    return fallback;
  }
}

export async function writeJson<T>(key: string, value: T) {
  try {
    await AsyncStorage.setItem(key, JSON.stringify(value));
  } catch {
    // Ignore transient local persistence failures so the app flow keeps working.
  }
}

export async function readText(key: string, fallback: string): Promise<string> {
  try {
    const raw = await AsyncStorage.getItem(key);
    return raw ?? fallback;
  } catch {
    return fallback;
  }
}

export async function writeText(key: string, value: string) {
  try {
    await AsyncStorage.setItem(key, value);
  } catch {
    // Ignore transient local persistence failures so the app flow keeps working.
  }
}
