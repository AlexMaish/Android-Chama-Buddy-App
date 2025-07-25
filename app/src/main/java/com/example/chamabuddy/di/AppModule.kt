package com.example.chamabuddy.di

import android.content.Context
import androidx.room.Room
import com.example.chamabuddy.data.local.*
import com.example.chamabuddy.data.repository.*
import com.example.chamabuddy.domain.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "mchama_database"
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideMemberDao(db: AppDatabase): MemberDao = db.memberDao()
    @Provides fun provideCycleDao(db: AppDatabase): CycleDao = db.cycleDao()
    @Provides fun provideMeetingDao(db: AppDatabase): WeeklyMeetingDao = db.meetingDao()
    @Provides fun provideBeneficiaryDao(db: AppDatabase): BeneficiaryDao = db.beneficiaryDao()
    @Provides fun provideContributionDao(db: AppDatabase): MemberContributionDao = db.contributionDao()
    @Provides fun provideMonthlySavingDao(db: AppDatabase): MonthlySavingDao = db.monthlySavingDao()
    @Provides fun provideSavingEntryDao(db: AppDatabase): MonthlySavingEntryDao = db.savingEntryDao()
    @Provides fun provideGroupDao(db: AppDatabase): GroupDao = db.groupDao()
    @Provides fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
    @Provides fun provideUserGroupDao(db: AppDatabase): UserGroupDao = db.userGroupDao()
    @Provides fun provideGroupMemberDao(db: AppDatabase): GroupMemberDao = db.groupMemberDao()

    @Provides fun provideExpenseDao(db: AppDatabase): ExpenseDao = db.expenseDao()
    @Provides fun provideBenefitDao(db: AppDatabase): BenefitDao = db.benefitDao()
    @Provides
    fun providePenaltyDao(database: AppDatabase): PenaltyDao = database.penaltyDao()



    /** DISPATCHER **/
    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    /** REPOSITORIES **/
    @Provides
    @Singleton
    fun provideUserRepository(
        userDao: UserDao,
        userGroupDao: UserGroupDao,
        memberRepository: MemberRepository,
        @ApplicationContext context: Context
    ): UserRepository = UserRepositoryImpl(
        userDao = userDao,
        userGroupDao = userGroupDao,
        memberRepository = memberRepository,
        context = context
    )

    @Provides
    @Singleton
    fun provideMemberRepository(
        memberDao: MemberDao,
        userDao: UserDao,
        userGroupDao: UserGroupDao
    ): MemberRepository = MemberRepositoryImpl(
        memberDao = memberDao,
        userDao = userDao,
        userGroupDao = userGroupDao
    )

    @Provides
    @Singleton
    fun provideCycleRepository(
        cycleDao: CycleDao,
        meetingDao: WeeklyMeetingDao,
        beneficiaryDao: BeneficiaryDao,
        memberDao: MemberDao,
        groupRepository: GroupRepository,
        dispatcher: CoroutineDispatcher
    ): CycleRepository = CycleRepositoryImpl(
        cycleDao = cycleDao,
        meetingDao = meetingDao,
        beneficiaryDao = beneficiaryDao,
        memberDao = memberDao,
        groupRepository = groupRepository,
        dispatcher = dispatcher
    )

    @Provides
    @Singleton
    fun provideMeetingRepository(
        meetingDao: WeeklyMeetingDao,
        contributionDao: MemberContributionDao,
        beneficiaryDao: BeneficiaryDao,
        memberDao: MemberDao,
        cycleDao: CycleDao,
        weeklyMeetingDao: WeeklyMeetingDao,
        dispatcher: CoroutineDispatcher
    ): MeetingRepository = MeetingRepositoryImpl(
        meetingDao = meetingDao,
        contributionDao = contributionDao,
        beneficiaryDao = beneficiaryDao,
        memberDao = memberDao,
        cycleDao = cycleDao,
        weeklyMeetingDao = weeklyMeetingDao,
        dispatcher = dispatcher
    )

    @Provides
    @Singleton
    fun provideBeneficiaryRepository(
        beneficiaryDao: BeneficiaryDao,
        dispatcher: CoroutineDispatcher
    ): BeneficiaryRepository = BeneficiaryRepositoryImpl(beneficiaryDao, dispatcher)

    @Provides
    @Singleton
    fun provideMemberContributionRepository(
        contributionDao: MemberContributionDao
    ): MemberContributionRepository = MemberContributionRepositoryImpl(contributionDao)

    @Provides
    @Singleton
    fun provideSavingsRepository(
        db: AppDatabase,
        savingDao: MonthlySavingDao,
        savingEntryDao: MonthlySavingEntryDao,
        cycleDao: CycleDao,
        memberDao: MemberDao,
        dispatcher: CoroutineDispatcher
    ): SavingsRepository = SavingsRepositoryImpl(
        db = db,
        savingDao = savingDao,
        savingEntryDao = savingEntryDao,
        cycleDao = cycleDao,
        memberDao = memberDao,
        dispatcher = dispatcher
    )

    @Provides
    @Singleton
    fun provideGroupRepository(
        groupDao: GroupDao,
        memberDao: MemberDao,
        userGroupDao: UserGroupDao,
        userRepository: UserRepository,
        groupMemberDao: GroupMemberDao
    ): GroupRepository = GroupRepositoryImpl(
        groupDao = groupDao,
        memberDao = memberDao,
        userGroupDao = userGroupDao,
        userRepository = userRepository,
        groupMemberDao = groupMemberDao
    )

    @Provides
    @Singleton
    fun provideBenfitRepository(
        benefitDao: BenefitDao
    ): BenefitRepository = BenefitRepositoryImpl (benefitDao)


    @Provides
    @Singleton
    fun providePenaltyRepository(
        penaltyDao: PenaltyDao
    ): PenaltyRepository = PenaltyRepositoryImpl(penaltyDao)



    @Provides
    @Singleton
    fun provideExpenseRepository(
        expenseDao: ExpenseDao
    ): ExpenseRepository = ExpenseRepositoryImpl(expenseDao)


}