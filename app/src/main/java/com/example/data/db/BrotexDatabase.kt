package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.SentinelLogEntity
import com.example.data.model.StudentEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [StudentEntity::class, AttendanceRecordEntity::class, SentinelLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BrotexDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun sentinelLogDao(): SentinelLogDao

    companion object {
        @Volatile
        private var INSTANCE: BrotexDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): BrotexDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BrotexDatabase::class.java,
                    "brotex_cbtis227.db"
                ).addCallback(BrotexDatabaseCallback(scope))
                 .fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class BrotexDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.studentDao())
                }
            }
        }

        suspend fun populateInitialData(studentDao: StudentDao) {
            val initialStudents = listOf(
                StudentEntity("22315050227001", "Alonso Vega Valeria", "IA5AM", "VEC_VAL_01", "AV", 0xFF00E5FF),
                StudentEntity("22315050227002", "Álvarez Morales Brandon", "IA5AM", "VEC_BRA_02", "AM", 0xFF00B0FF),
                StudentEntity("22315050227003", "Barrera Castillo Gael", "IA5AM", "VEC_GAE_03", "BC", 0xFF1DE9B6),
                StudentEntity("22315050227004", "Bautista Reyes Jimena", "IA5AM", "VEC_JIM_04", "BR", 0xFFFF4081),
                StudentEntity("22315050227005", "Cabrera Fuentes Emilio", "IA5AM", "VEC_EMI_05", "CF", 0xFF7C4DFF),
                StudentEntity("22315050227006", "Camacho Rivera Mariana", "IA5AM", "VEC_MAR_06", "CR", 0xFF00E676),
                StudentEntity("22315050227007", "Cárdenas Ruiz Santiago", "IA5AM", "VEC_SAN_07", "CR", 0xFFFF9100),
                StudentEntity("22315050227008", "Corona Delgado Andrea", "IA5AM", "VEC_AND_08", "CD", 0xFFE040FB),
                StudentEntity("22315050227009", "Domínguez Ortiz Leonardo", "IA5AM", "VEC_LEO_09", "DO", 0xFF00B8D4),
                StudentEntity("22315050227010", "Estrada Flores Regina", "IA5AM", "VEC_REG_10", "EF", 0xFFFF5252),
                StudentEntity("22315050227011", "Fuentes Mendoza Diego", "IA5AM", "VEC_DIE_11", "FM", 0xFF448AFF),
                StudentEntity("22315050227012", "García Navarro Sofia", "IA5AM", "VEC_SOF_12", "GN", 0xFF69F0AE),
                StudentEntity("22315050227013", "Gómez Serrano Mateo", "IA5AM", "VEC_MAT_13", "GS", 0xFFFFAB40),
                StudentEntity("22315050227014", "Hernández Cruz Camila", "IA5AM", "VEC_CAM_14", "HC", 0xFFFF6E40),
                StudentEntity("22315050227015", "Ibáñez Romero Sebastián", "IA5AM", "VEC_SEB_15", "IR", 0xFF536DFE),
                StudentEntity("22315050227016", "Juárez Pacheco Daniela", "IA5AM", "VEC_DAN_16", "JP", 0xFFFF80AB),
                StudentEntity("22315050227017", "Lara Villanueva Rodrigo", "IA5AM", "VEC_ROD_17", "LV", 0xFF64FFDA),
                StudentEntity("22315050227018", "López Aguilar Valentina", "IA5AM", "VEC_VAL_18", "LA", 0xFFB388FF),
                StudentEntity("22315050227019", "Martínez Solís Alejandro", "IA5AM", "VEC_ALE_19", "MS", 0xFF00E5FF),
                StudentEntity("22315050227020", "Medina Ramos Fernanda", "IA5AM", "VEC_FER_20", "MR", 0xFFFFD740),
                StudentEntity("22315050227021", "Molina Vargas Carlos", "IA5AM", "VEC_CAR_21", "MV", 0xFF40C4FF),
                StudentEntity("22315050227022", "Navarro Paredes Renata", "IA5AM", "VEC_REN_22", "NP", 0xFFFF5252),
                StudentEntity("22315050227023", "Orozco Peña Emiliano", "IA5AM", "VEC_EMI_23", "OP", 0xFF7C4DFF),
                StudentEntity("22315050227024", "Pérez Benítez Ximena", "IA5AM", "VEC_XIM_24", "PB", 0xFF00E676),
                StudentEntity("22315050227025", "Ramírez Guzmán Daniel", "IA5AM", "VEC_DAN_25", "RG", 0xFFFF9100),
                StudentEntity("22315050227026", "Ríos Valencia Natalia", "IA5AM", "VEC_NAT_26", "RV", 0xFF1DE9B6),
                StudentEntity("22315050227027", "Sánchez Trejo Ángel", "IA5AM", "VEC_ANG_27", "ST", 0xFF00B0FF),
                StudentEntity("22315050227028", "Torres Márquez Paula", "IA5AM", "VEC_PAU_28", "TM", 0xFFFF4081),
                StudentEntity("22315050227029", "Vázquez Luna Ian", "IA5AM", "VEC_IAN_29", "VL", 0xFF64FFDA),
                StudentEntity("22315050227030", "Zúñiga Morales Estefanía", "IA5AM", "VEC_EST_30", "ZM", 0xFFFFD740)
            )
            studentDao.insertStudents(initialStudents)
        }
    }
}
